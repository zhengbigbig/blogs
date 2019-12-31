package hello.service;


import hello.dao.BlogDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BlogServiceTest {
    @Mock
    private BlogDao blogDao;
    @InjectMocks
    private BlogService blogService;

    @Test
    void testGetBlogs() {
        blogService.getBlogs(1, 1, 1);
        verify(blogDao).getBlogs(1, 1, 1);
    }
}
