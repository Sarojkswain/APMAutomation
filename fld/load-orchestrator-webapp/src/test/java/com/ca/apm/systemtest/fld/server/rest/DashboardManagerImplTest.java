package com.ca.apm.systemtest.fld.server.rest;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.dozer.DozerBeanMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import com.ca.apm.systemtest.fld.server.dao.DashboardDao;
import com.ca.apm.systemtest.fld.server.manager.DashboardManager;
import com.ca.apm.systemtest.fld.server.manager.DashboardManagerImpl;
import com.ca.apm.systemtest.fld.server.model.ConfigItem;
import com.ca.apm.systemtest.fld.server.model.Dashboard;
import com.ca.apm.systemtest.fld.server.model.DashboardConfig;
import com.ca.apm.systemtest.fld.server.model.EnumConfigItem;
import com.ca.apm.systemtest.fld.shared.vo.ConfigItemVO;
import com.ca.apm.systemtest.fld.shared.vo.DashboardVO;

/**
 * Unit test suite class for {@link DashboardManager}.
 *  
 * @author sinal04
 *
 */
public class DashboardManagerImplTest {

    private DashboardManagerImpl dashboardManagerImpl;
    
    
    @Before
    public void setup() throws Exception {
        dashboardManagerImpl = new DashboardManagerImpl();
        DashboardDao dashboardDao = mock(DashboardDao.class); 
        when(dashboardDao.find(1L)).thenReturn(createTestDashboard());
        doNothing().when(dashboardDao).update(any(Dashboard.class));
        dashboardManagerImpl.setDashboardDao(dashboardDao);

        RepositoryService repositoryService = mock(RepositoryService.class);
        ProcessDefinition procDef = mock(ProcessDefinition.class);
        when(procDef.getVersion()).thenReturn(1);
        ProcessDefinitionQuery procDefQuery = mock(ProcessDefinitionQuery.class);
        when(procDefQuery.singleResult()).thenReturn(procDef);
        when(procDefQuery.processDefinitionKey(any(String.class))).thenReturn(procDefQuery);
        when(procDefQuery.latestVersion()).thenReturn(procDefQuery);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(procDefQuery);
        dashboardManagerImpl.setRepositoryService(repositoryService);
        
        DozerBeanMapper mapper = new DozerBeanMapper(Collections.singletonList("dozer-config.xml"));
        dashboardManagerImpl.setMapper(mapper);
    }

    @After
    public void teardown() throws Exception {
    }

    @Test
    public void testUpdateDashboardWithEnumConfigParams() {
        DashboardVO dashboardVO = new DashboardVO();
        dashboardVO.setId(1L);
        dashboardVO.setName("TestDashboard1");
        dashboardVO.setProcessKey("test.process");
        List<ConfigItemVO> configItemVOs = new ArrayList<>();
        dashboardVO.setConfig(configItemVOs);
        ConfigItemVO enumConfigItemVO = new ConfigItemVO();
        configItemVOs.add(enumConfigItemVO);
        enumConfigItemVO.setFormId("config.enum.item");
        enumConfigItemVO.setName("configEnumItem");
        enumConfigItemVO.setTypeInformation("{key1=val1,key2=val2}");
        enumConfigItemVO.setValue("val1");
        enumConfigItemVO.setRequired(true);
        enumConfigItemVO.setType("enum");
        
        Dashboard updatedDashboard = dashboardManagerImpl.updateDashboardFromVO(dashboardVO);
        Assert.notNull(updatedDashboard);
        Assert.notNull(updatedDashboard.getDashboardConfig());
        Assert.notNull(updatedDashboard.getDashboardConfig().getConfigItems());
        Assert.notEmpty(updatedDashboard.getDashboardConfig().getConfigItems());
        Assert.isInstanceOf(EnumConfigItem.class, updatedDashboard.getDashboardConfig().getConfigItems().get(0));
        EnumConfigItem enumConfigItem = (EnumConfigItem) updatedDashboard.getDashboardConfig().getConfigItems().get(0);
        Assert.notNull(enumConfigItem.getEnumValues());
    }
    
    private Dashboard createTestDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        dashboard.setName("TestDashboard");
        dashboard.setProcessKey("test.process");
        dashboard.setProcessDefinitionVersion(1);
        
        DashboardConfig config = new DashboardConfig();
        dashboard.setDashboardConfig(config);
        config.setId(1L);
        config.setVersion(1L);
        List<ConfigItem> configItems = new ArrayList<>();
        config.setConfigItems(configItems);
        
        ConfigItem enumConfigItem = new EnumConfigItem("configEnumItem", "config.enum.item", "val1", "{key1=val1,key2=val2}");
        enumConfigItem.setRequired(true);
        configItems.add(enumConfigItem);
        
        return dashboard;
    }

}
