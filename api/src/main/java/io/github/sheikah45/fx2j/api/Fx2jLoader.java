package io.github.sheikah45.fx2j.api;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadListener;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "unused"})
public class Fx2jLoader {

    private static final List<Fx2jBuilderFinder> BUILDER_FINDERS;
    private static final boolean FALL_BACK_TO_FXML;

    static {
        BUILDER_FINDERS = ServiceLoader.load(Fx2jBuilderFinder.class)
                                       .stream()
                                       .map(ServiceLoader.Provider::get)
                                       .toList();

        boolean fxmlLoaderExists;
        try {
            Class.forName("javafx.fxml.FXMLLoader", false, Fx2jLoader.class.getClassLoader());
            fxmlLoaderExists = true;
        } catch (ClassNotFoundException e) {
            fxmlLoaderExists = false;
        }
        FALL_BACK_TO_FXML = fxmlLoaderExists;
    }


    private URL location;
    private ResourceBundle resources;
    private Object root;
    private Object controller;
    private Function<Class<?>, Builder<?>> builderFactory;
    private Function<Class<?>, Object> controllerFactory;
    private Charset charset;
    private ClassLoader classLoader;
    private LoadListener loadListener;

    public URL getLocation() {
        return location;
    }

    public void setLocation(URL location) {
        this.location = location;
    }

    public ResourceBundle getResources() {
        return resources;
    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public <T> T getRoot() {
        return (T) root;
    }

    public void setRoot(Object root) {
        this.root = root;
    }

    public <T> T getController() {
        return (T) controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Function<Class<?>, Builder<?>> getBuilderFactory() {
        return builderFactory;
    }

    /**
     * Only used for FXML fallback loader
     *
     * @see FXMLLoader#setBuilderFactory(BuilderFactory)
     */
    public void setBuilderFactory(Function<Class<?>, Builder<?>> builderFactory) {
        this.builderFactory = builderFactory;
    }

    public Function<Class<?>, Object> getControllerFactory() {
        return controllerFactory;
    }

    public void setControllerFactory(Function<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Only used for FXML fallback loader
     *
     * @see FXMLLoader#setCharset(Charset)
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Only used for FXML fallback loader
     *
     * @see FXMLLoader#setClassLoader(ClassLoader)
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public LoadListener getLoadListener() {
        return loadListener;
    }

    /**
     * Only used for FXML fallback loader
     *
     * @see FXMLLoader#setLoadListener(LoadListener)
     */
    public void setLoadListener(LoadListener loadListener) {
        this.loadListener = loadListener;
    }

    public <T> T load() throws IOException {
        for (Fx2jBuilderFinder finder : BUILDER_FINDERS) {
            Fx2jBuilder<? super Object, ? super Object> builder = (Fx2jBuilder<? super Object, ? super Object>) finder.findBuilder(
                    location);
            if (builder != null) {
                builder.build(controller, root, resources, controllerFactory);
                setController(builder.getController());
                setRoot(builder.getRoot());
                return (T) builder.getRoot();
            }
        }

        if (FALL_BACK_TO_FXML) {
            return loadFromFxml();
        }

        throw new IllegalArgumentException("Cannot find builder for location %s".formatted(getLocation()));
    }

    private <T> T loadFromFxml() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        if (resources != null) {
            fxmlLoader.setResources(resources);
        }
        if (location != null) {
            fxmlLoader.setLocation(location);
        }
        if (controller != null) {
            fxmlLoader.setController(controller);
        }
        if (classLoader != null) {
            fxmlLoader.setClassLoader(classLoader);
        }
        if (charset != null) {
            fxmlLoader.setCharset(charset);
        }
        if (loadListener != null) {
            fxmlLoader.setLoadListener(loadListener);
        }
        if (root != null) {
            fxmlLoader.setRoot(root);
        }
        if (controllerFactory != null) {
            fxmlLoader.setControllerFactory(controllerFactory::apply);
        }
        if (builderFactory != null) {
            fxmlLoader.setBuilderFactory(builderFactory::apply);
        }

        fxmlLoader.load();
        setController(fxmlLoader.getController());
        setRoot(fxmlLoader.getRoot());

        return fxmlLoader.getRoot();
    }
}
