package com.example.Refi;

import com.example.Refi.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.*;

@RestController
@RequestMapping(path = "/api/v1/roles", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class Controller {
    @Autowired
    private DealTypeConfig dealFieldTypes;

    @Value("#{${eod}}")
    private Map<String, String> dealData;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/")
    public DealTypeDTO test(@NotNull @RequestParam String name) throws JsonProcessingException {
        DealTypeDTO dealTypeDTO = new DealTypeDTO();
        List<DealFieldTypeDTO>  dealFieldTypeDTOS = validateAndGetFieldTypes();
//
        dealTypeDTO.setDealTypeName(name);
        dealTypeDTO.setDefault(true);

        List<DealTypeFieldDTO> dealTypeFieldDTOSs = new ArrayList<>();
        for(String s : dealData.keySet()){
            DealTypeFieldDTO field = new DealTypeFieldDTO();
            field.setOrder(100L);
            field.setDealFieldType(getFieldType(dealFieldTypeDTOS, s));
            if(field.getDealFieldType() == null){
                System.out.println(s);
            }
            String[] permissions = dealData.get(s).split("\\|");
            field.setEditable(Boolean.parseBoolean(permissions[0]));
            field.setMandatoryVisible(Boolean.parseBoolean(permissions[1]));
            field.setHidden(Boolean.parseBoolean(permissions[2]));

            dealTypeDTO.getDealTypeField().add(field);

        }

        //objectMapper.readValue("", DealType.class);

        return dealTypeDTO;
    }

    private DealFieldTypeDTO getFieldType(List<DealFieldTypeDTO> types, String type){
        for (DealFieldTypeDTO dft : types){
            if(dft.getLabel().equals(type)){
                return dft;
            }
        }
        return null;
    }

    private List<DealFieldTypeDTO> validateAndGetFieldTypes() throws JsonProcessingException {
        return new LinkedList<>(Arrays.asList(
                objectMapper.readValue(dealFieldTypes.getDealFieldTypes(), DealFieldTypeDTO[].class)));

    }
}
