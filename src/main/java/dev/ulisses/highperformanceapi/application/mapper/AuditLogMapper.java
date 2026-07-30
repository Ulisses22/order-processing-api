package dev.ulisses.highperformanceapi.application.mapper;

import dev.ulisses.highperformanceapi.application.dto.response.AuditLogResponse;
import dev.ulisses.highperformanceapi.domain.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AuditLogMapper {

    @Mapping(target = "performedBy", source = "username")
    AuditLogResponse toResponse(AuditLog auditLog);

}
