package fi.jubic.quanta.external;

import dagger.Module;
import dagger.Provides;
import fi.jubic.quanta.external.importer.MainImporter;
import fi.jubic.quanta.external.ingester.MainIngester;

import javax.inject.Singleton;

@Module
public class ExternalModule {
    @Provides
    @Singleton
    static Importer provideImporter(MainImporter importer) {
        return importer;
    }

    @Provides
    @Singleton
    static Ingester provideIngester(MainIngester ingester) {
        return ingester;
    }
}
