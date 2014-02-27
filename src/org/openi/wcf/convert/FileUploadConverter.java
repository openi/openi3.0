/*
 * ====================================================================
 * Copyright (c) 2003 TONBELLER AG.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        TONBELLER AG (http://www.tonbeller.com)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TON BELLER AG OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * 
 */
package org.openi.wcf.convert;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.tonbeller.wcf.convert.ConvertException;
import com.tonbeller.wcf.format.Formatter;
import com.tonbeller.wcf.ui.FileUploadCtrl;
import com.tonbeller.wcf.utils.XoplonNS;

public class FileUploadConverter extends NodeConverterBase {

  private static Logger logger = Logger.getLogger(FileUploadConverter.class);

  public void convert(Formatter fmt, Map param, Map fileSource, Element element, Object bean)
      throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    if (FileUploadCtrl.isDisabled(element))
      return;

    String id = FileUploadCtrl.getId(element);

    FileItem [] fileItems = (FileItem[]) fileSource.get(id);

    // input available
    if (fileItems != null && fileItems.length>0) {
      FileItem fileItem = fileItems[0];
      try {
        XoplonNS.removeAttribute(element, "error");

        FileUploadCtrl.setFileName(element, fileItem.getName());

        String model = FileUploadCtrl.getModelReference(element);
        if (bean != null && model.length() > 0) {
          PropertyUtils.setProperty(bean, model, fileItem);
        }
      } catch (IllegalAccessException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", fileItem.getName());
        throw e;
      } catch (NoSuchMethodException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", fileItem.getName());
        throw e;
      } catch (InvocationTargetException e) {
        logger.info("exception caught", e);
        XoplonNS.setAttribute(element, "error", e.getMessage());
        XoplonNS.setAttribute(element, "value", fileItem.getName());
        throw e;
      }
    }
  }

  public void convert(Formatter fmt, Object bean, Element element) throws ConvertException,
      IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    try {
      String model = FileUploadCtrl.getModelReference(element);
      if (model.length() == 0)
        return;

      FileItem value = (FileItem) PropertyUtils.getProperty(bean, model);
      if(value!=null)
        FileUploadCtrl.setFileName(element, value.getName());
      else
        FileUploadCtrl.setFileName(element, "");

    } catch (IllegalAccessException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    } catch (NoSuchMethodException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    } catch (InvocationTargetException e) {
      XoplonNS.setAttribute(element, "error", e.getMessage());
      throw e;
    }
  }

}