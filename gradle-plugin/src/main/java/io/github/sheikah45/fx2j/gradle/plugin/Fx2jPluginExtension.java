package io.github.sheikah45.fx2j.gradle.plugin;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

public interface Fx2jPluginExtension {
    Property<String> getBaseSourceSetName();
    Property<String> getBasePackage();
    Property<Boolean> getStrict();

    Property<Boolean> getModularizeIfPossible();
    SetProperty<String> getExcludes();
    SetProperty<String> getIncludes();
}
