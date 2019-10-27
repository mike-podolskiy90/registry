package org.gbif.registry.surety.email;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static freemarker.template.Configuration.VERSION_2_3_25;

/**
 * Email template processor allows to generate a {@link BaseEmailModel} from a Freemarker template.
 */
public abstract class FreemarkerEmailTemplateProcessor implements EmailTemplateProcessor {

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  // shared config among all instances
  private static final Configuration FREEMARKER_CONFIG = new Configuration(VERSION_2_3_25);

  static {
    FREEMARKER_CONFIG.setDefaultEncoding(StandardCharsets.UTF_8.name());
    FREEMARKER_CONFIG.setLocale(Locale.US);
    FREEMARKER_CONFIG.setNumberFormat("0.####");
    FREEMARKER_CONFIG.setDateFormat("yyyy-mm-dd");
    FREEMARKER_CONFIG.setClassForTemplateLoading(FreemarkerEmailTemplateProcessor.class,
        "/email");
  }

  public BaseEmailModel buildEmail(EmailType emailType, String emailAddress, Object templateDataModel, @Nullable Locale locale)
      throws IOException, TemplateException {
    return buildEmail(emailType, emailAddress, templateDataModel, locale, null);
  }

  /**
   * Build a {@link BaseEmailModel} from
   *
   * @param emailType         template type (new user, reset password or welcome)
   * @param emailAddress
   * @param templateDataModel
   * @param locale            if null is provided {@link #DEFAULT_LOCALE} will be used
   * @return
   * @throws IOException
   * @throws TemplateException
   */
  public BaseEmailModel buildEmail(EmailType emailType, String emailAddress, Object templateDataModel, @Nullable Locale locale,
                                   List<String> ccAddresses)
      throws IOException, TemplateException {

    Objects.requireNonNull(emailAddress, "emailAddress shall be provided");
    Objects.requireNonNull(templateDataModel, "templateDataModel shall be provided");

    //at some point this class should be able to check supported locale
    Locale emailLocale = Optional.ofNullable(locale).orElse(DEFAULT_LOCALE);

    // Prepare the E-Mail body text
    StringWriter contentBuffer = new StringWriter();
    FREEMARKER_CONFIG.getTemplate(getEmailDataProvider().getTemplate(emailLocale, emailType)).process(templateDataModel, contentBuffer);
    return new BaseEmailModel(emailAddress, getEmailDataProvider().getSubject(emailLocale, emailType), contentBuffer.toString(), ccAddresses);
  }

  public abstract EmailDataProvider getEmailDataProvider();
}
