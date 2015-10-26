package core.framework.impl.template.html.node;

import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.ContainerFragment;
import core.framework.impl.template.source.TemplateSource;

/**
 * @author neo
 */
public interface Node {
    void buildTemplate(ContainerFragment fragment, CallTypeStack stack, TemplateSource source);
}