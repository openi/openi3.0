package org.openi.wcf.form;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.openi.util.xml.XmlUtils;
import org.w3c.dom.Document;

import com.tonbeller.wcf.component.Component;
import com.tonbeller.wcf.component.ComponentTag;
import com.tonbeller.wcf.controller.RequestContext;
import com.tonbeller.wcf.form.FormComponent;
import com.tonbeller.wcf.wizard.WizardComponent;
import com.tonbeller.wcf.wizard.WizardComponentTag;

/**
 * creates a FormComponent
 *
 * @author av
 * @author SUJEN
 */
public class FormComponentTag extends ComponentTag {

  String xmlUri;
  String model;
  boolean bookmarkable = false;
  boolean finishButton = true;
  String bundle;

  private static Logger logger = Logger.getLogger(FormComponentTag.class);

  public Component createComponent(RequestContext context) throws JspException {
    try {

      Document doc = parseDocument(context, getXmlUri());

      // find the bean model
      Object bean = null;
      if (model != null)
        bean = context.getModelReference(model);

      // create the component
      FormComponent fc = createFormComponent(context, id, doc, bean);
      fc.setBookmarkable(bookmarkable);
      fc.setFinishButton(finishButton);

      registerWithWizard(fc);
      return fc;

    } catch (MalformedURLException e) {
      logger.error(null, e);
      throw new JspException(e);
    }
  }

  /**
   * if this is used inside a wizard tag, then its registered with the wizard.
   */
  private void registerWithWizard(FormComponent fc) {
    WizardComponentTag wt = (WizardComponentTag) findAncestorWithClass(this,
        WizardComponentTag.class);
    if (wt == null)
      return;
    WizardComponent wc = (WizardComponent) wt.getComponent();
    wc.addPage(fc);
  }

  protected FormComponent createFormComponent(RequestContext context, String id, Document doc,
      Object bean) {
    return new FormComponent(id, null, doc, bean);
  }

  protected Document parseDocument(RequestContext context, String xmlUri) throws MalformedURLException {
    Document document = XmlUtils.parse(new File(xmlUri).toURL()); 
    return document;
  }

  public String getXmlUri() {
    return xmlUri;
  }

  public void setXmlUri(String xmlUri) {
    this.xmlUri = xmlUri;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setBookmarkable(boolean b) {
    bookmarkable = b;
  }

  public void setBundle(String bundle) {
    this.bundle = bundle;
  }

  public boolean isFinishButton() {
    return finishButton;
  }

  public void setFinishButton(boolean finishButton) {
    this.finishButton = finishButton;
  }

  public String getBundle() {
    return bundle;
  }

}
