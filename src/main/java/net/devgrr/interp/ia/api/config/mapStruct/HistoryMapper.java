package net.devgrr.interp.ia.api.config.mapStruct;

import net.devgrr.interp.ia.api.work.history.dto.HistoryResponse;
import net.devgrr.interp.ia.api.work.history.entity.History;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
  HistoryResponse toResponse(History history);
}
