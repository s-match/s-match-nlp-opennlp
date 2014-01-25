package it.unitn.disi.nlptools.components.postaggers;

import it.unitn.disi.common.DISIException;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.components.ConfigurationKeyMissingException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.pipelines.LabelPipelineComponent;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Tags the label using OpenNLP POS tagger.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OpenNLPPOSTagger extends LabelPipelineComponent {

    private static final Logger log = LoggerFactory.getLogger(OpenNLPPOSTagger.class);

    private static final String MODEL_FILE_NAME_KEY = "model";

    private POSTaggerME tagger;

    public void process(ILabel instance) {
        String tokens[] = new String[instance.getTokens().size()];
        for (int i = 0; i < instance.getTokens().size(); i++) {
            tokens[i] = instance.getTokens().get(i).getText();
        }
        String[] tags = tagger.tag(tokens);
        for (int i = 0; i < instance.getTokens().size(); i++) {
            instance.getTokens().get(i).setPOSTag(tags[i]);
        }
    }

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        if (log.isInfoEnabled()) {
            log.info("Loading configuration...");
        }
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(MODEL_FILE_NAME_KEY)) {
                String modelFileName = (String) newProperties.get(MODEL_FILE_NAME_KEY);
                if (log.isInfoEnabled()) {
                    log.info("Loading model: " + modelFileName);
                }

                InputStream modelIn = null;
                try {
                    modelIn = MiscUtils.getInputStream(modelFileName);
                    POSModel model = new POSModel(modelIn);
                    tagger = new POSTaggerME(model);
                } catch (IOException e) {
                    throw new ConfigurableException(e.getMessage(), e);
                } catch (DISIException e) {
                    throw new ConfigurableException(e.getMessage(), e);
                } finally {
                    if (modelIn != null) {
                        try {
                            modelIn.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            } else {
                throw new ConfigurationKeyMissingException(MODEL_FILE_NAME_KEY);
            }
        }
        return result;
    }
}