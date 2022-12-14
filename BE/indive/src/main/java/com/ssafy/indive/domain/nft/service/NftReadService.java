package com.ssafy.indive.domain.nft.service;

import com.ssafy.indive.domain.member.entity.Member;
import com.ssafy.indive.domain.member.repository.MemberRepository;
import com.ssafy.indive.domain.nft.entity.Nft;
import com.ssafy.indive.domain.nft.exception.NftNotFoundException;
import com.ssafy.indive.domain.nft.repository.NftQueryRepository;
import com.ssafy.indive.domain.nft.service.dto.ServiceCheckAmountGetRequestDto;
import com.ssafy.indive.domain.nft.service.dto.ServiceCheckStockGetRequestDto;
import com.ssafy.indive.domain.nft.service.dto.ServiceNftAmountResponseDto;
import com.ssafy.indive.domain.nft.service.dto.ServiceNftImageGetRequestDto;
import com.ssafy.indive.global.blockchain.InDiveNFT;
import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.io.IOException;
import java.util.Optional;

import static com.ssafy.indive.global.constant.BlockchainConst.*;
import static com.ssafy.indive.global.constant.BlockchainConst.INDIVENFT_ADDRESS;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NftReadService {

    private final MemberRepository memberRepository;

    private final NftQueryRepository nftQueryRepository;

    public ServiceNftAmountResponseDto checkStock(ServiceCheckStockGetRequestDto dto) {
        Member artist = memberRepository.findById(dto.getArtistSeq()).orElseThrow(IllegalArgumentException::new);

        Nft nft = nftQueryRepository.findByArtist(artist).orElseThrow(() -> new NftNotFoundException("false"));

        return new ServiceNftAmountResponseDto(nft.getLowerDonationAmount());
    }

    public boolean checkAmount(ServiceCheckAmountGetRequestDto dto) {
        Member artist = memberRepository.findById(dto.getArtistSeq()).orElseThrow(IllegalArgumentException::new);

        Nft nft = nftQueryRepository.findByArtist(artist).orElseThrow(() -> new NftNotFoundException("?????? ??????????????? NFT??? ????????????."));

        return dto.getAmount() >= nft.getLowerDonationAmount();
    }

    public Resource getImage(ServiceNftImageGetRequestDto dto) throws IOException {
        IPFS ipfs = new IPFS("/ip4/3.34.252.202/tcp/5001");

        Multihash filePointer = Multihash.fromBase58(dto.getCid());

        byte[] content = ipfs.cat(filePointer);

        return new ByteArrayResource(content);
    }
}
