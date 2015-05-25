package com.smartbear.rapisupport;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;

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
                BuildTestSuite(project, restService);
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

    public static WsdlTestSuite BuildTestSuite(WsdlProject project, RestService restService) {
        RestRequestStepFactory factory = new RestRequestStepFactory();
        WsdlTestSuite suite = project.addNewTestSuite("TestSuite " + restService.getName());
        WsdlTestCase testCase = suite.addNewTestCase("TestCase 1");
        for (RestResource resource: restService.getAllOperations()) {
            for (int i = 0; i < resource.getRequestCount(); i++) {
                RestRequest request = resource.getRequestAt(i);
                testCase.addTestStep(factory.createConfig(request, "Step" + i));
            }
        }
        return suite;
    }
}
