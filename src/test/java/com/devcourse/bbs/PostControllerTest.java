package com.devcourse.bbs;

import com.devcourse.bbs.controller.bind.PostCreateRequest;
import com.devcourse.bbs.controller.bind.PostUpdateRequest;
import com.devcourse.bbs.domain.post.Post;
import com.devcourse.bbs.domain.user.User;
import com.devcourse.bbs.repository.post.PostRepository;
import com.devcourse.bbs.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    ObjectMapper objectMapper;

    User user;

    private static final FieldDescriptor apiResponseSuccess = fieldWithPath("success").description("Status of result");
    private static final FieldDescriptor apiResponseResult = fieldWithPath("result").description("Value of result");
    private static final FieldDescriptor apiResponseError = fieldWithPath("error").description("Message of result");

    @BeforeEach
    void initUser() {
        user = userRepository.save(User.builder()
                .name("NAME")
                .age(25)
                .hobby("HOBBY")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build());
    }

    @Test
    void createPostTest() throws Exception {
        PostCreateRequest request = new PostCreateRequest();
        request.setUser(user.getName());
        request.setTitle("TITLE");
        request.setContent("CONTENT");
        mockMvc.perform(post("/posts")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$['result'].title").value("TITLE"))
                .andExpect(jsonPath("$['result'].content").value("CONTENT"))
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andDo(document("PostController/createPost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("title").description("Title text of post"),
                                fieldWithPath("content").description("Content text of post"),
                                fieldWithPath("user").description("Username of post's writer")),
                        responseFields(
                                apiResponseSuccess,
                                apiResponseResult,
                                apiResponseError,
                                fieldWithPath("result.id").description("Id of post"),
                                fieldWithPath("result.title").description("Title of post"),
                                fieldWithPath("result.content").description("Content of post"),
                                subsectionWithPath("result.user").description("Information about post's writer"))
                ));
    }

    @Test
    void updatePostTest() throws Exception {
        Post post = postRepository.save(Post.builder()
                .title("TITLE")
                .content("CONTENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("UPDATED_TITLE");
        request.setContent("UPDATED_CONTENT");
        mockMvc.perform(put("/posts/{id}", post.getId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['result'].title").value("UPDATED_TITLE"))
                .andExpect(jsonPath("$['result'].content").value("UPDATED_CONTENT"))
                .andDo(document("PostController/updatePost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID of post")),
                        requestFields(
                                fieldWithPath("title").description("Title text of post"),
                                fieldWithPath("content").description("Content text of post")),
                        responseFields(
                                apiResponseSuccess,
                                apiResponseResult,
                                apiResponseError,
                                fieldWithPath("result.id").description("Id of post"),
                                fieldWithPath("result.title").description("Updated title of post"),
                                fieldWithPath("result.content").description("Updated content of post"),
                                subsectionWithPath("result.user").description("Information about post's writer"))
                ));
    }

    @Test
    void deletePostTest() throws Exception {
        Post post = postRepository.save(Post.builder()
                .title("TITLE")
                .content("CONTENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        mockMvc.perform(delete("/posts/{id}", post.getId()))
                .andExpect(status().isNoContent())
                .andDo(document("PostController/deletePost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID of post"))));
    }

    @Test
    void getPostTest() throws Exception {
        Post post = postRepository.save(Post.builder()
                .title("TITLE")
                .content("CONTENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['result'].title").value("TITLE"))
                .andExpect(jsonPath("$['result'].content").value("CONTENT"))
                .andDo(document("PostController/readPost",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("Id of post.")),
                        responseFields(
                                apiResponseSuccess,
                                apiResponseResult,
                                apiResponseError,
                                fieldWithPath("result.id").description("Id of post"),
                                fieldWithPath("result.title").description("Title of post"),
                                fieldWithPath("result.content").description("Content of post"),
                                subsectionWithPath("result.user").description("Information about post's writer"))
                ));
    }

    @Test
    void readPostsByPageTest() throws Exception {
        postRepository.save(Post.builder()
                .title("TITLE1")
                .content("CONTENT1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        postRepository.save(Post.builder()
                .title("TITLE2")
                .content("CONTENT2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        Post post3 = postRepository.save(Post.builder()
                .title("TITLE3")
                .content("CONTENT3")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user).build());
        mockMvc.perform(get("/posts")
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['result']").isArray())
                .andExpect(jsonPath("$['result'][0].id").value(post3.getId()))
                .andDo(
                        document("PostController/readPostsByPage",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestParameters(
                                        parameterWithName("page").description("Page number of post pagination."),
                                        parameterWithName("size").description("Page size of post pagination.")),
                                responseFields(
                                        apiResponseSuccess,
                                        apiResponseResult,
                                        apiResponseError,
                                        fieldWithPath("result.[]").description("Posts"),
                                        fieldWithPath("result.[].id").description("Id of post"),
                                        fieldWithPath("result.[].title").description("Updated title of post"),
                                        fieldWithPath("result.[].content").description("Updated content of post"),
                                        subsectionWithPath("result.[].user").description("Information about post's writer"))));
    }
}