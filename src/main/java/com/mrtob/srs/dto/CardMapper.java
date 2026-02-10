package com.mrtob.srs.dto;

import com.mrtob.srs.entity.Card;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    CardResponse toResponse(Card card);

    List<CardResponse> toResponseList(List<Card> cards);
}
