package it.unitn.disi.nlptools.components.tokenizers;

import it.unitn.disi.common.DISIException;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.components.ConfigurationKeyMissingException;
import it.unitn.disi.common.utils.MiscUtils;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.IToken;
import it.unitn.disi.nlptools.data.Token;
import it.unitn.disi.nlptools.pipelines.LabelPipelineComponent;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Tokenizes the label using OpenNLP tokenizer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OpenNLPTokenizer extends LabelPipelineComponent {

    private static final Logger log = LoggerFactory.getLogger(OpenNLPTokenizer.class);

    private static final String MODEL_FILE_NAME_KEY = "model";

    private Tokenizer tokenizer;

    public void process(ILabel instance) {
        String tokens[] = tokenizer.tokenize(instance.getText());
        List<IToken> tokenList = new ArrayList<IToken>(tokens.length);
        for (String token : tokens) {
            tokenList.add(new Token(token));
        }
        instance.setTokens(tokenList);
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
                    TokenizerModel model = new TokenizerModel(modelIn);
                    tokenizer = new TokenizerME(model);
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