// 环境变量
String buildNumber = env.BUILD_NUMBER;
String timestamp = new Date().format('yyyyMMddHHmmss');
String projectName = env.JOB_NAME.split(/\//)[0];
// e.g awesome-project/release/RELEASE-1.0.0
String branchName = env.JOB_NAME.split(/\//)[1..-1].join(/\//);

println("${buildNumber} ${timestamp} ${projectName}");

String version = "${buildNumber}-${timestamp}-${projectName}";

node {
    checkout scm; // 从源代码管理系统拉下来最新的代码

    if(params.BuildType=='Rollback') { // 如果选择类型为rollback，则执行rollback()
        return rollback()
    } else if(params.BuildType=='Normal'){
        return normalCIBuild(version)
    } else if(branchName == 'master'){ // 扫描到主分支时创建俩种构建类型
        setScmPollStrategyAndBuildTypes(['Normal', 'Rollback']);
    }
}

def normalCIBuild(String version) {
    stage 'test & package'  // 执行jenkins 的test 和 package阶段

    sh('./mvnw clean package')

    stage('docker build') // 对docker打包

    sh("docker build . -t 18.177.139.235:5000/test:${version}")

    sh("docker push 18.177.139.235:5000/test:${version}")

    stage('deploy') // 部署

    input 'deploy?'

    deployVersion(version)
}

def deployVersion(String version) {
    sh "ssh aws 'docker rm -f test && docker run --name test -d -p 8080:8080 18.177.139.235:5000/test:${version}'"
}

def setScmPollStrategyAndBuildTypes(List buildTypes) {
    // 告诉Jenkinsfile俩种构建类型，克隆表达式，每分钟检查git仓库，如果有更新则会自动拉取
    def propertiesArray = [
            parameters([choice(choices: buildTypes.join('\n'), description: '', name: 'BuildType')]),
            pipelineTriggers([[$class: "SCMTrigger", scmpoll_spec: "* * * * *"]])
    ];
    properties(propertiesArray);
}

def rollback() {
    def dockerRegistryHost = "http://47.103.56.219:5000";
    def getAllTagsUri = "/v2/test/tags/list";

    def responseJson = new URL("${dockerRegistryHost}${getAllTagsUri}")
            .getText(requestProperties: ['Content-Type': "application/json"]);

    println(responseJson)

    // {name:xxx,tags:[tag1,tag2,...]}
    Map response = new groovy.json.JsonSlurperClassic().parseText(responseJson) as Map;

    def versionsStr = response.tags.join('\n');

    def rollbackVersion = input(
            message: 'Select a version to rollback',
            ok: 'OK',
            parameters: [choice(choices: versionsStr, description: 'version', name: 'version')])

    println rollbackVersion
    deployVersion(rollbackVersion)
}