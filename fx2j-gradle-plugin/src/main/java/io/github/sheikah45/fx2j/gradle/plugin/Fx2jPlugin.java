package io.github.sheikah45.fx2j.gradle.plugin;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class Fx2jPlugin implements Plugin<Project> {

    private static final String FX2J = "fx2j";

    @Override
    public void apply(Project project) {

        ProjectLayout layout = project.getLayout();
        Fx2jPluginExtension extension = project.getExtensions().create(FX2J, Fx2jPluginExtension.class);
        extension.getBaseSourceSetName().convention("main");
        extension.getBasePackage().convention("fx2j.builder");
        extension.getStrict().convention(false);
        extension.getModularizeIfPossible().convention(true);
        extension.getExcludes().convention(Set.of());
        extension.getIncludes().convention(Set.of());

        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSetContainer sourceSets = javaPluginExtension.getSourceSets();
        ConfigurationContainer configurations = project.getConfigurations();

        Provider<SourceSet> baseSourceSetProvider = extension.getBaseSourceSetName().map(sourceSets::getByName);
        Provider<Configuration> baseCompileClassPathConfiguration = baseSourceSetProvider.map(
                                                                                                 SourceSet::getCompileClasspathConfigurationName)
                                                                                         .map(configurations::getByName);
        Provider<Configuration> baseRuntimeClassPathConfiguration = baseSourceSetProvider.map(
                                                                                                 SourceSet::getRuntimeClasspathConfigurationName)
                                                                                         .map(configurations::getByName);


        TaskContainer tasks = project.getTasks();
        TaskProvider<CompileFx2jTask> compileFxmlTask = tasks.register("compileFx2j", CompileFx2jTask.class);

        NamedDomainObjectProvider<SourceSet> fx2jSourceSet = sourceSets.register(FX2J, sourceSet -> {
            SourceSet baseSourceSet = baseSourceSetProvider.get();
            FileCollection baseSourceSetOutput = baseSourceSet.getOutput();

            FileCollection compileClasspath = baseSourceSet.getCompileClasspath().plus(baseSourceSetOutput);
            sourceSet.setCompileClasspath(compileClasspath);

            FileCollection runtimeClasspath = baseSourceSet.getRuntimeClasspath().plus(baseSourceSetOutput);
            sourceSet.setRuntimeClasspath(runtimeClasspath);

            configurations.named(sourceSet.getCompileClasspathConfigurationName(),
                                 configuration -> configuration.extendsFrom(baseCompileClassPathConfiguration.get()));
            configurations.named(sourceSet.getRuntimeClasspathConfigurationName(),
                                 configuration -> configuration.extendsFrom(baseRuntimeClassPathConfiguration.get()));
            baseSourceSet.setRuntimeClasspath(baseSourceSet.getRuntimeClasspath()
                                                           .plus(project.files(sourceSet.getOutput())));
            baseSourceSetProvider.map(SourceSet::getRuntimeOnlyConfigurationName);

            tasks.named(sourceSet.getCompileJavaTaskName()).configure(task -> task.dependsOn(compileFxmlTask));
        });

        compileFxmlTask.configure(task -> {
            task.setGroup(FX2J);
            task.dependsOn(Set.of(baseSourceSetProvider.map(SourceSet::getCompileJavaTaskName)));
            task.getExcludes().set(extension.getExcludes());
            task.getIncludes().set(extension.getIncludes());
            task.getStrict().set(extension.getStrict());
            task.getModularizeIfPossible().set(extension.getModularizeIfPossible());

            task.getInputSourceSet().set(baseSourceSetProvider);
            task.getOutputSourceSet().set(fx2jSourceSet);

            task.getFxmlBuilderPackage().set(extension.getBasePackage());
        });

        TaskProvider<Jar> fx2jJarTask = tasks.register(fx2jSourceSet.map(SourceSet::getJarTaskName)
                                                                    .get(), Jar.class, task -> {
            task.setGroup(FX2J);
            task.dependsOn(fx2jSourceSet.map(SourceSet::getClassesTaskName));
            task.from(fx2jSourceSet.map(SourceSet::getOutput));
            task.manifest(manifest -> manifest.attributes(Map.of("Automatic-Module-Name", extension.getBasePackage())));
            task.getArchiveClassifier().set(FX2J);

            SourceSet baseSourceSet = baseSourceSetProvider.get();
            project.getDependencies()
                   .add(baseSourceSet.getRuntimeOnlyConfigurationName(),
                        project.getLayout().files(task.getArchiveFile()));
        });

        TaskProvider<Delete> cleanFx2jTask = tasks.register("cleanFx2j", Delete.class, task -> {
            task.setGroup(FX2J);
            task.delete(fx2jSourceSet.map(SourceSet::getAllSource));
        });

        tasks.named("build").configure(build -> build.dependsOn(fx2jJarTask));
        tasks.named("clean").configure(clean -> clean.dependsOn(cleanFx2jTask));
    }
}
