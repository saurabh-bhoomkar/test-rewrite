To prevent unwanted XML namespace attributes from being added to the output XML in a JAXB-based project, you can take the following steps:

---

### Solution 1: Use `Marshaller` Properties to Suppress Namespaces
JAXB's `Marshaller` provides properties to customize the XML output. You can remove the unnecessary namespaces by setting the appropriate properties.

Here’s an example:

```java
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

public class Main {
    public static void main(String[] args) throws Exception {
        JAXBContext context = JAXBContext.newInstance(MyDto.class); // Replace with your JAXB-annotated DTO
        Marshaller marshaller = context.createMarshaller();

        // Prevent namespace prefixes from appearing
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CustomNamespacePrefixMapper());

        MyDto myDto = new MyDto(); // Populate your DTO
        marshaller.marshal(myDto, System.out);
    }
}
```

You can define a custom `NamespacePrefixMapper` to suppress unnecessary namespaces:

```java
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class CustomNamespacePrefixMapper extends NamespacePrefixMapper {
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        // Return "" (empty string) to suppress the namespace
        return "";
    }
}
```

---

### Solution 2: Use `@XmlRootElement` and Customize the Namespace
If your XML output includes unwanted namespace attributes like `xmlns=""`, this might occur due to `@XmlRootElement` annotations. Ensure the namespace in the `@XmlRootElement` annotation is explicitly set or omitted.

```java
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MyDto", namespace = "")
public class MyDto {
    // Your fields and methods here
}
```

Setting the `namespace` to an empty string ensures no namespace attributes appear in the output.

---

### Solution 3: Use a `NamespaceFilter` SAX Filter
If you need complete control over namespace processing, you can use a `SAX` filter. This approach involves filtering namespaces during the serialization process.

1. Define a `NamespaceFilter`:

```java
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.*;

public class NamespaceFilter extends XMLFilterImpl {
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement("", localName, qName, attributes); // Suppress namespaces
    }
}
```

2. Use it with the `Marshaller`:

```java
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.xml.sax.XMLFilter;

public class Main {
    public static void main(String[] args) throws Exception {
        JAXBContext context = JAXBContext.newInstance(MyDto.class);
        Marshaller marshaller = context.createMarshaller();

        MyDto myDto = new MyDto(); // Populate your DTO

        NamespaceFilter filter = new NamespaceFilter();
        filter.setParent(new DefaultHandler());
        marshaller.marshal(myDto, filter);
    }
}
```

---

### Solution 4: Use `@XmlSchema` at the Package Level
You can control the namespaces across all classes in a package by defining a `package-info.java` file:

```java
@jakarta.xml.bind.annotation.XmlSchema(
    namespace = "", // Set to empty string to suppress namespace
    elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.UNQUALIFIED
)
package com.example.dto;
```

This approach applies to all JAXB-annotated classes in the `com.example.dto` package.

---

### Solution 5: Custom `Marshaller.Listener`
JAXB `Marshaller.Listener` allows for more fine-grained control of the marshalling process. You can implement a custom listener to strip out unwanted namespaces dynamically.

---

### Recommendation:
For simplicity and maintainability, start with **Solution 1** (custom `NamespacePrefixMapper`) or **Solution 4** (package-level annotations). These methods are efficient and minimize code changes.

Let me know if you need additional details or specific examples!
