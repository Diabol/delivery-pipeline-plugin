@XmlJavaTypeAdapter(type = Status.class, value = XmlStatusAdapter.class)
package se.diabol.jenkins.pipeline.model;

import se.diabol.jenkins.pipeline.model.status.Status;
import se.diabol.jenkins.pipeline.model.status.XmlStatusAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;