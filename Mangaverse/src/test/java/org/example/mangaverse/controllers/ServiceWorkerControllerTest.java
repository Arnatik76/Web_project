package org.example.mangaverse.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.mangaverse.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ServiceWorkerController.class)
@Import(SecurityConfig.class)
class ServiceWorkerControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserDetailsService userDetailsService;

    @Test
    void servedFromRootAndNeverCached() throws Exception {
        mvc.perform(get("/sw.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/javascript"))
                .andExpect(header().string("Cache-Control", "no-cache"));
    }
}
