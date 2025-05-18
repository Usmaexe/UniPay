package com.unipay.mapper;


import com.unipay.dto.BusinessDto;
import com.unipay.models.Business;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class BusinessMapper {

    public abstract BusinessDto toDto(Business business);
}
