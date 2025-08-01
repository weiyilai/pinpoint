package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.web.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.web.uid.service.ServiceUidCachedService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/uid")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidController {

    private final ServiceUidCachedService serviceUidCachedService;
    private final ApplicationUidService applicationUidService;

    public ApplicationUidController(ServiceUidCachedService serviceUidCachedService, ApplicationUidService applicationUidService) {
        this.serviceUidCachedService = Objects.requireNonNull(serviceUidCachedService, "serviceUidCachedService");
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "cachedApplicationUidService");
    }

    @GetMapping(value = "/applications")
    public List<ApplicationUidAttribute> getApplications(@RequestParam(value = "serviceName", required = false) String serviceName) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        return applicationUidService.getApplicationNames(serviceUid);
    }

    @GetMapping(value = "/debug/application", params = "applicationName")
    public ResponseEntity<Long> getApplicationUid(@RequestParam(value = "serviceName", required = false) String serviceName,
                                                  @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                                  @RequestParam(value = "serviceTypeCode") int serviceTypeCode) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        ApplicationUid applicationUid = applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (applicationUid == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationUid.getUid());
    }

    @GetMapping(value = "/debug/application", params = "applicationUid")
    public ResponseEntity<ApplicationUidAttribute> getApplicationName(@RequestParam(value = "serviceName", required = false) String serviceName,
                                                                      @RequestParam(value = "applicationUid") long applicationUid) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        ApplicationUid applicationUidObject = ApplicationUid.of(applicationUid);
        ApplicationUidAttribute applicationUidAttribute = applicationUidService.getApplication(serviceUid, applicationUidObject);
        if (applicationUidAttribute == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applicationUidAttribute);
    }

    @DeleteMapping(value = "/application")
    public ResponseEntity<String> deleteApplication(@RequestParam(value = "serviceName", required = false) String serviceName,
                                                        @RequestParam(value = "applicationName") @NotBlank String applicationName,
                                                        @RequestParam(value = "serviceTypeCode") int serviceTypeCode) {
        ServiceUid serviceUid = getServiceUid(serviceName);
        applicationUidService.deleteApplication(serviceUid, applicationName, serviceTypeCode);
        return ResponseEntity.ok("OK");
    }

    private ServiceUid getServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        return serviceUidCachedService.getServiceUid(serviceName);
    }

}
