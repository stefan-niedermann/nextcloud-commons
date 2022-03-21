package it.niedermann.android.markdown.markwon.plugins;

import androidx.annotation.NonNull;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import it.niedermann.android.markdown.markwon.processor.NextcloudImageDestinationProcessor;

public class RelativeImageUrlPlugin extends AbstractMarkwonPlugin {

    private NextcloudImageDestinationProcessor processor = new NextcloudImageDestinationProcessor();


    public static MarkwonPlugin create() { return new RelativeImageUrlPlugin(); }

    @Override
    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
        builder.imageDestinationProcessor(processor);
    }

    public void setImagePrefix(@NonNull String prefix) {
        processor.setPrefix(prefix);
    }

}
