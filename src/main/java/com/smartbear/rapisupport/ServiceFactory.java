package com.smartbear.rapisupport;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;

import java.util.List;
import java.util.Set;

public class ServiceFactory {
    private ServiceFactory() {
    }

    public static void Build(WsdlProject project, List<RestService> services, Set<Service> entities) {
        if (entities.isEmpty()) {
            return;
        }

        for (RestService restService : services) {
            if (entities.contains(Service.TEST_SUITE)) {
                //TODO: not implemented
            }
            if (entities.contains(Service.LOAD_TEST)) {
                //TODO: not implemented
            }
            if (entities.contains(Service.SECUR_TEST)) {
                //TODO: not implemented
            }
            if (entities.contains(Service.VIRT)) {
                //TODO: not implemented
            }
        }
    }
}
