package org.bremersee.profile.test.model;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Bremer
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BufferSchemaOutputResolver extends SchemaOutputResolver {

    private Map<String, StreamResult> buffers = new HashMap<>();

    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        StringWriter out = new StringWriter();
        StreamResult res = new StreamResult(out);
        res.setSystemId(suggestedFileName);
        buffers.put(namespaceUri, res);
        return res;
    }

    public Set<String> getNamespaces() {
        return buffers.keySet();
    }

    public String getSchema(String namespaceUri) {
        return buffers.get(namespaceUri).getWriter().toString();
    }

    public String getSystemId(String namespaceUri) {
        return buffers.get(namespaceUri).getSystemId();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : buffers.keySet()) {
            sb.append("***** Begin schema ").append(s).append(", system-id=").append(getSystemId(s)).append(" *****");
            sb.append(System.getProperty("line.separator"));
            sb.append(getSchema(s));
            sb.append("***** End schema ").append(s).append(" *****");
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public void clear() {
        buffers.clear();
    }

}
