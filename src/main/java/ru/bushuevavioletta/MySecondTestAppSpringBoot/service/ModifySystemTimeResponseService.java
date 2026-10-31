package ru.bushuevavioletta.MySecondTestAppSpringBoot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.Response;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.util.DateTimeUtil;

import java.util.Date;

@Slf4j
@Service
@Qualifier("ModifySystemTimeResponseService")
public class ModifySystemTimeResponseService implements ModifyResponseService {

    @Override
    public Response modify(Response response) {

        response.setSystemTime(DateTimeUtil.getCustomFormat().format(new Date()));
        log.info("ModifySystemTimeResponseService: systemTime изменён на {}",
                response.getSystemTime());

        return response;
    }
}