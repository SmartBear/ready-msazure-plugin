package com.smartbear.rapisupport;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.model.load.LoadTestModelItem;
import com.eviware.soapui.model.testsuite.TestSuite;

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
            WsdlTestSuite testSuite = null;
            if (entities.contains(Service.TEST_SUITE)) {
                testSuite = BuildTestSuite(project, restService);
            }
            if (testSuite != null && entities.contains(Service.LOAD_TEST)) {
                BuildLoadTest(project, testSuite);
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

    public static void BuildLoadTest(WsdlProject project, WsdlTestSuite testSuite) {
        final int testCaseCount = testSuite.getTestCaseCount();
        for (int i = 0; i < testCaseCount; i++) {
            WsdlTestCase nextTestCase = testSuite.getTestCaseAt(i);
            String name = nextTestCase.getLabel() + " LoadTest";
            name = findNextLoadTestName(testSuite, name);
            LoadTestModelItem loadUITest = nextTestCase.getProject().addNewLoadUITest(name);
            loadUITest.getSettings().setString(LoadTestModelItem.SOAPUI_OBJECT_SOURCE_ID, nextTestCase.getId());
            loadUITest.getSettings().setString(LoadTestModelItem.SOAPUI_OBJECT_SOURCE_TYPE, LoadTestModelItem.SOAPUI_OBJECT_SOURCE_TYPE_TESTCASE);
        }
    }

    private static String findNextLoadTestName(TestSuite suite, String newName) {
        if (!checkIfLoadTestNameExists(suite, newName)) {
            return newName;
        }

        boolean isNewName = false;
        String indexedName = null;
        for (int index = 1; !isNewName; index++) {
            indexedName = String.format("%s %d", newName, index);
            isNewName = !checkIfLoadTestNameExists(suite, indexedName);
        }
        return  indexedName;
    }

    private static boolean checkIfLoadTestNameExists(TestSuite suite, String newName) {
        for (LoadTestModelItem loadTest : suite.getProject().getLoadUITestList()) {
            if (loadTest.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }
}
