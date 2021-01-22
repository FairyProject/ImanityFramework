/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.config.yaml;

import com.google.common.jimfs.Jimfs;
import org.imanity.framework.config.classes.TestClass;
import org.imanity.framework.config.yaml.YamlConfiguration.YamlProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.imanity.framework.config.util.CollectionFactory.mapOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

class YamlSourceTest {
    private FileSystem fileSystem;
    private Path configPath;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem();
        configPath = fileSystem.getPath("/a/b/config.yml");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void yamlSourceCreatesDirectories() throws IOException {
        YamlSource source = new YamlSource(configPath, YamlProperties.DEFAULT);
        Path parentDir = configPath.getParent();
        assertThat(Files.exists(parentDir), is(false));
        source.saveConfiguration(new TestClass(configPath), mapOf());
        assertThat(Files.exists(parentDir), is(true));
    }
}