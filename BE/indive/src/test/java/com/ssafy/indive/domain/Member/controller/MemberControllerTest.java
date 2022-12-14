package com.ssafy.indive.domain.Member.controller;

import com.ssafy.indive.domain.member.controller.MemberController;
import com.ssafy.indive.domain.member.controller.dto.WebMemberWriteNoticeRequestDto;
import com.ssafy.indive.domain.member.entity.enumeration.Role;
import com.ssafy.indive.domain.member.service.MemberAddService;
import com.ssafy.indive.domain.member.service.MemberBlockchainService;
import com.ssafy.indive.domain.member.service.MemberModifyService;
import com.ssafy.indive.domain.member.service.MemberReadService;
import com.ssafy.indive.domain.member.service.dto.*;
import com.ssafy.indive.utils.JacksonUtil;
import com.ssafy.indive.utils.security.factory.WithMockSecurityContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ssafy.indive.utils.JacksonUtil.convertToJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc
@DisplayName("?????? ???????????? ?????? ?????????")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberAddService memberAddService;

    @MockBean
    private MemberReadService memberReadService;

    @MockBean
    private MemberModifyService memberModifyService;

    @MockBean
    private MemberBlockchainService memberBlockchainService;


    @Test
    @WithMockUser
    @DisplayName("[?????? ??????] ???????????? ????????? ????????? ??? ????????? ??????.")
    public void addMember() throws Exception {

        // given
        ServiceMemberAddRequestDto dto = ServiceMemberAddRequestDto.builder()
                .email("test@test")
                .nickname("test-name")
                .password("test-password")
                .profileMessage("test-message")
                .wallet("test-wallet")
                .build();

        given(memberAddService.addMember(any(ServiceMemberAddRequestDto.class))).willReturn(true);
        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/members/join")
                .content(convertToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
          );

        // then
        verify(memberAddService, times(1)).addMember(any(ServiceMemberAddRequestDto.class));

        actions.andExpect(content().string("true"));
        actions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ???????????? ????????? ????????????.")
    public void isDuplicated() throws Exception {
        //given
        given(memberReadService.isDuplicated(any(ServiceDuplicatedEmail.class))).willReturn(true);
        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/duplicated-email")
                        .param("email","test@test")

                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );
        //then
        verify(memberReadService, times(1)).isDuplicated(any(ServiceDuplicatedEmail.class));

        actions.andExpect(content().string("true"));
        actions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("????????? ??????????????? ????????????")
    public void getMemberDetails() throws Exception{
        //given


        ServiceMemberGetResponseDto responsedto = ServiceMemberGetResponseDto.builder()
                .memberSeq(1)
                .email("test@test")
                .nickname("test-nickname")
                .role(Role.ROLE_USER)
                .wallet("test-wallet")
                .profileMessage("test-profileMessage")
                .notice("test-notice")
                .build();

        Mockito.when(memberReadService.getMemberDetails(1)).thenReturn(responsedto);

        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        //then

        actions.andExpect(status().isOk());
        actions.andExpect(content().string(convertToJson(responsedto)));
    }

    @Nested
    @DisplayName("[?????? ??????] ???????????? ?????? ????????? ????????? ??? ??????.")
    class ModifyMember{

        @BeforeEach
        void beforeEach() {
            WithMockSecurityContextFactory.createSecurityContext();
        }


        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ???????????? ?????? ????????? ????????? ??? ??????..")
        public void success() throws Exception{
            //given

            MockMultipartFile image = new MockMultipartFile("file", "image.png", "image/png", Files.newInputStream(Paths.get(ClassLoader.getSystemResource("image.png").toURI())));
            MockMultipartFile background = new MockMultipartFile("file", "background.png", "image/png", Files.newInputStream(Paths.get(ClassLoader.getSystemResource("background.png").toURI())));

            given(memberModifyService.modifyMember(eq(1L),any(ServiceMemberModifyRequestDto.class))).willReturn(true);
            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.multipart("/members/1")
                    .file(image)
                    .file(background)
                    .param("nickname","test-nickname")
                    .param("profile-message","test-profile-message")
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    })
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            //then
            verify(memberModifyService, times(1)).modifyMember(eq(1L), any(ServiceMemberModifyRequestDto.class));

            actions.andExpect(content().string("true"));
            actions.andExpect(status().isOk());


        }

        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ?????? SEQ??? ????????? ?????? ??????")
        public void failure() throws Exception {
            MockMultipartFile image = new MockMultipartFile("file", "image.png", "image/png", Files.newInputStream(Paths.get(ClassLoader.getSystemResource("image.png").toURI())));
            MockMultipartFile background = new MockMultipartFile("file", "background.png", "image/png", Files.newInputStream(Paths.get(ClassLoader.getSystemResource("background.png").toURI())));

            given(memberModifyService.modifyMember(eq(1L),any(ServiceMemberModifyRequestDto.class))).willThrow(new IllegalArgumentException("?????? ?????? ?????? ??????????????????."));

            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.multipart("/members/1")
                    .file(image)
                    .file(background)
                    .param("nickname","test-nickname")
                    .param("profile-message","test-profile-message")
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    })
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            //then
            verify(memberModifyService, times(1)).modifyMember(eq(1L), any(ServiceMemberModifyRequestDto.class));

            actions.andExpect(content().string("?????? ?????? ?????? ??????????????????."));
            actions.andExpect(status().isBadRequest());
        }



    }



    @Test
    @WithMockUser
    @DisplayName("???????????? ????????? ?????? ????????? ????????????.")
    public void getLoginMemberDetails() throws Exception{
        ServiceMemberGetResponseDto responsedto = ServiceMemberGetResponseDto.builder()
                .memberSeq(1)
                .email("test@test")
                .nickname("test-nickname")
                .role(Role.ROLE_USER)
                .wallet("test-wallet")
                .profileMessage("test-profileMessage")
                .notice("test-notice")
                .build();

        Mockito.when(memberReadService.getLoginMemberDetails()).thenReturn(responsedto);

        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/my-account")
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        //then
        actions.andExpect(status().isOk());
        actions.andExpect(content().string(convertToJson(responsedto)));


    }

    @Test
    @WithMockUser
    @DisplayName("??????????????? ????????????.")
    public void writeNotice() throws Exception{
        //given
        WebMemberWriteNoticeRequestDto dto = WebMemberWriteNoticeRequestDto.builder()
                        .notice("test-notice")
                        .build();

        given(memberModifyService.writeNotice(any(ServiceMemberWriteNoticeRequestDto.class), eq(1L))).willReturn(true);

        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post("/members/1/notice")
                .content(convertToJson(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        //then
        verify(memberModifyService, times(1)).writeNotice(any(ServiceMemberWriteNoticeRequestDto.class),eq(1L));

        actions.andExpect(content().string("true"));
        actions.andExpect(status().isOk());

    }

    @Nested
    @DisplayName("[??????????????? ????????????]")
    public class DownloadProfile{
        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ????????? ???????????? ????????? ??????????????????.")
        public void success() throws Exception{
            //given
            given(memberReadService.downloadProfileImage(eq(1L))).willReturn(new UrlResource(ClassLoader.getSystemResource("image.png").toURI()));

            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/1/profileimg-download")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));
            //then
            verify(memberReadService, times(1)).downloadProfileImage(eq(1L));

            actions.andExpect(header().string("Content-Type", "image/png"));
            actions.andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ?????? ????????? ????????? ????????? SEQ ??? ?????? ??????.")
        public void failure() throws Exception{
            //given
            given(memberReadService.downloadProfileImage(eq(1L))).willThrow(IllegalArgumentException.class);

            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/1/profileimg-download")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));
            //then
            verify(memberReadService, times(1)).downloadProfileImage(eq(1L));

            actions.andExpect(content().string("?????? ?????? ?????? ??????????????????."));
            actions.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("[???????????? ????????????]")
    public class DownloadBackground{
    @Test
    @WithMockUser
    @DisplayName("[?????? ?????????] ?????? ???????????? ????????? ??????????????????.")
    public void success() throws Exception{

        //given
        given(memberReadService.downloadBackgroundImage(eq(1L))).willReturn(new UrlResource(ClassLoader.getSystemResource("image.png").toURI()));

        //when
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/1/backgroundimg-download")
                .with(SecurityMockMvcRequestPostProcessors.csrf()));
        //then
        verify(memberReadService,times(1)).downloadBackgroundImage(eq(1L));

        actions.andExpect(header().string("Content-Type", "image/png"));
        actions.andExpect(status().isOk());

    }

        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ?????? ???????????? ????????? ????????? SEQ ??? ?????? ??????.")
        public void failure() throws Exception{

            //given
            given(memberReadService.downloadBackgroundImage(eq(1L))).willThrow(IllegalArgumentException.class);

            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/1/backgroundimg-download")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));
            //then
            verify(memberReadService,times(1)).downloadBackgroundImage(eq(1L));

            actions.andExpect(content().string("?????? ?????? ?????? ??????????????????."));
            actions.andExpect(status().isBadRequest());

        }
    }

    @Nested
    @DisplayName("[?????? ??????]")
    public class GetDonationRanking{
        List<ServiceMemberDonationRankResponseDto> list;
        @BeforeEach
        public void beforeEach() {
           list = new ArrayList<>();
            ServiceMemberDonationRankResponseDto dto = ServiceMemberDonationRankResponseDto.builder()
                    .memberSeq(1L)
                    .nickname("test-nickname")
                    .address("test-address")
                    .totalValue(100)
                    .build();

            list.add(dto);
        }
        @Test
        @WithMockUser
        @DisplayName("[?????? ?????????] ?????? ?????? ????????? ????????????.")
        public void success() throws Exception{
            //given
            given(memberBlockchainService.getDonationRanking(any(String.class))).willReturn(list);

            //when
            ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/members/donation-rank/1")
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));

            //then
            actions.andExpect(content().json(JacksonUtil.convertToJson(list)));
            actions.andExpect(status().isOk());
        }

    }

}