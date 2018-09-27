package com.ca.fld.flex.sample;

import flex.messaging.io.ClassAliasRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.jmeter.protocol.amf.util.AmfXmlConverter;

/**
 * @author meler02
 */
public class FlexParser {

    public static void main(String[] args) throws Exception {
        int retval = new FlexParser().run(args);
        System.exit(retval);
    }

    void convertOneFile(Path filePath) throws IOException {
        byte[] data = Files.readAllBytes(filePath);
        //System.out.println(new String(data));
        ClassAliasRegistry aliases = ClassAliasRegistry.getRegistry();
        aliases.registerAlias("DSK", "flex.messaging.messages.AcknowledgeMessageExt");
        aliases.registerAlias("DSC", "flex.messaging.messages.CommandMessageExt");
        String xml = AmfXmlConverter.convertAmfMessageToXml(data, true);
        byte[] amfBinary = AmfXmlConverter.convertXmlToAmfMessage(xml);
        assert amfBinary != null;
        //System.out.println(xml);
        Path destPath = Paths.get(filePath.toAbsolutePath() + ".xml");
        Files.write(destPath, xml.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    int run(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("converts AMF binary files to XStream XML files");
            System.out.println("$0 file1.amf file2.amf ... fileN.amf");
            return 1;
        }

        for (String path : args) {
            Path filePath = Paths.get(path);
            if (filePath.toFile().exists()) {
                System.out
                    .print(String.format("Converting %s...", filePath.toAbsolutePath().toString()));
                convertOneFile(filePath);
                System.out.println("done.");
            }
        }

        return 0;
    }
}
