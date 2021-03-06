package info.atende.nserver.config;


import info.atende.nserver.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Giovanni Silva
 *         10/09/15.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    Logger logger = LoggerFactory.getLogger(WebConfig.class);

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/", "classpath:/resources/",
            "classpath:/static/", "classpath:/public/"};
    @Autowired
    private Environment env;

    @Value("${app.version:}")
    private String appVersion;


    /**
     * Views sem controller
     *
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/logout").setViewName("logout");
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/app/**").setViewName("forward:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    private String getCurrentFileLocation() {
        File file = new File(".");
        return file.getAbsolutePath();
    }

    private String getClientFolderLocation() {
        String currentFile = getCurrentFileLocation();
        if (currentFile.endsWith(".")) {
            currentFile = currentFile.substring(0, currentFile.length() - 1);
        }
        File file = new File(currentFile + "/client");

        return "file:///" + file.getAbsolutePath() + "/";
    }

    private String[] getDevLocations() {
        List<String> resources = new ArrayList<>();
        for (String r : CLASSPATH_RESOURCE_LOCATIONS) {
            resources.add(r);
        }
        String clientFolderLocation = getClientFolderLocation();
        logger.info("Client Folder Location: " + clientFolderLocation);
        resources.add(clientFolderLocation + "dist/");
        return resources.toArray(new String[resources.size()]);
    }

    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String[] locations = null;
        if (devMode()) {
            locations = getDevLocations();
        } else {
            locations = CLASSPATH_RESOURCE_LOCATIONS;
        }
        Integer cachePeriod = devMode() ? 0 : null;
        boolean useResourceCache = !devMode();
        String version = getApplicationVersion();

        AppCacheManifestTransformer appCacheTransformer = new AppCacheManifestTransformer();
        VersionResourceResolver versionResolver = new VersionResourceResolver()
                .addFixedVersionStrategy(version, "/**/*.js", "/**/*.map")
                .addContentVersionStrategy("/**");
        if (locations.length > 0)
            registry.addResourceHandler("/**")
                    .addResourceLocations(locations)
                    .setCachePeriod(cachePeriod)
                    .resourceChain(useResourceCache)
                    .addResolver(versionResolver)
                    .addTransformer(appCacheTransformer);
    }

    protected String getApplicationVersion() {
        return this.devMode() ? "dev" : this.appVersion;
    }
    // Internationalization and Locale

    @Bean(name = "messageSource")
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource;
        final String baseName = "classpath:/i18n/messages";
        final String encoding = "UTF-8";

        messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename(baseName);
        messageSource.setDefaultEncoding(encoding);

        return messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("language"); // Url to change url be ?language=en
        registry.addInterceptor(interceptor);
    }


    /**
     * Verifica se aplicacao esta no perfil development
     *
     * @return
     */
    private boolean devMode() {
        return this.env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT);
    }
}

