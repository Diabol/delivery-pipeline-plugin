@XmlJavaTypeAdapter(type = Status.class, value = XmlStatusAdapter.class)
package com.diabol.pipefitter.model;

import com.diabol.pipefitter.model.status.Status;
import com.diabol.pipefitter.model.status.XmlStatusAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;