/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.web.controller;

import org.jtalks.common.model.entity.Component;
import org.jtalks.common.service.exceptions.NotFoundException;
import org.jtalks.jcommune.model.entity.PluginConfiguration;
import org.jtalks.jcommune.model.entity.PluginConfigurationProperty;
import org.jtalks.jcommune.model.plugins.Plugin;
import org.jtalks.jcommune.service.ComponentService;
import org.jtalks.jcommune.service.PluginService;
import org.jtalks.jcommune.service.dto.PluginActivatingListDto;
import org.jtalks.jcommune.service.dto.PluginActivatingDto;
import org.mockito.Mock;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeAvailable;
import static org.springframework.test.web.ModelAndViewAssert.assertViewName;
import static org.testng.Assert.assertEquals;

/**
 *
 * @author Anuar Nurmakanov
 */
public class PluginControllerTest {

    @Mock
    private PluginService pluginService;
    @Mock
    private ComponentService componentService;

    private PluginController pluginController;

    @BeforeMethod
    public void init() {
        initMocks(this);
        this.pluginController = new PluginController(pluginService, componentService);
    }

    @Test
    public void getPluginsShouldReturnAllPlugins() {
        long componentId = 25L;
        Component component = new Component();
        component.setId(componentId);
        when(componentService.getComponentOfForum()).thenReturn(component);
        List<Plugin> expectedPlugins = Arrays.asList((Plugin) new DummyPlugin(), new DummyPlugin());
        when(pluginService.getPlugins(componentId)).thenReturn(expectedPlugins);

        ModelAndView pluginsModelAndView = pluginController.getPlugins();

        assertViewName(pluginsModelAndView, "pluginList");
        assertModelAttributeAvailable(pluginsModelAndView, "plugins");
        assertModelAttributeAvailable(pluginsModelAndView, "pluginsActivatingListDto");
        List<Plugin> actualPlugins = (List<Plugin>) pluginsModelAndView.getModel().get("plugins");
        assertEquals(actualPlugins, expectedPlugins, "Plugins should be returned from services.");
    }

    @Test
    public void configurePluginShouldMoveToPluginConfigurationPage() throws NotFoundException {
        String configuredPluginName = "plugin";
        PluginConfiguration expectedConfiguration = new PluginConfiguration();
        long componentId = 25L;
        Component component = new Component();
        component.setId(componentId);
        when(componentService.getComponentOfForum()).thenReturn(component);
        when(pluginService.getPluginConfiguration(configuredPluginName, componentId)).thenReturn(expectedConfiguration);

        ModelAndView pluginConfigModelAndView = pluginController.configurePlugin(configuredPluginName);

        assertViewName(pluginConfigModelAndView, "pluginConfiguration");
        assertModelAttributeAvailable(pluginConfigModelAndView, "pluginConfiguration");
        PluginConfiguration actualPluginConfiguration = (PluginConfiguration) pluginConfigModelAndView.getModel().get("pluginConfiguration");
        assertEquals(actualPluginConfiguration, expectedConfiguration, "Plugin should be returned from services.");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void configurePluginWhenPluginWasNotFoundShouldShowNotFoundError() throws NotFoundException {
        long componentId = 25L;
        Component component = new Component();
        component.setId(componentId);
        when(componentService.getComponentOfForum()).thenReturn(component);
        String nonExistPluginName = "non-exist";
        when(pluginService.getPluginConfiguration(nonExistPluginName, componentId)).thenThrow(new NotFoundException());

        pluginController.configurePlugin(nonExistPluginName);
    }

    @Test
    public void updateConfigurationShouldUpdateItByCallingServiceLayer() throws NotFoundException {
        long componentId = 25L;
        Component component = new Component();
        component.setId(componentId);
        when(componentService.getComponentOfForum()).thenReturn(component);
        String pluginName = "plugin";
        PluginConfiguration newConfiguration = new PluginConfiguration();
        newConfiguration.setName(pluginName);

        ModelAndView resultModelAndView = pluginController.updateConfiguration(newConfiguration);

        assertViewName(resultModelAndView, "/plugins/configure/" + pluginName);
        assertModelAttributeAvailable(resultModelAndView, "pluginConfiguration");
        verify(pluginService).updateConfiguration(newConfiguration, componentId);
    }

    @Test
    public void updateEnablingShouldUpdateAllPassedPlugins() throws NotFoundException {
        long componentId = 25L;
        Component component = new Component();
        component.setId(componentId);
        when(componentService.getComponentOfForum()).thenReturn(component);
        List<PluginActivatingDto> pluginActivatingDtoList = new ArrayList<>();
        PluginActivatingListDto pluginsEnablingDto = new PluginActivatingListDto(pluginActivatingDtoList);

        String destinationUrl = pluginController.updateActivating(pluginsEnablingDto);

        assertEquals(destinationUrl, "/plugins/list", "After correct update of plugins enabling, user should see updated plugins");
        verify(pluginService).updatePluginsActivating(pluginActivatingDtoList, componentId);
    }

    /**
     * Created for tests plugin.
     */
    private static final class DummyPlugin implements Plugin {

        @Override
        public boolean supportsJCommuneVersion(String version) {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public List getConfiguration() {
            return null;
        }

        @Override
        public List<PluginConfigurationProperty> getDefaultConfiguration() {
            return null;
        }

        @Override
        public void configure(PluginConfiguration configuration) {

        }

        @Override
        public State getState() {
            return null;
        }
    }
}