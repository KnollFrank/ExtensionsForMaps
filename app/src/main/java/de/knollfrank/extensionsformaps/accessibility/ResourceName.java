package de.knollfrank.extensionsformaps.accessibility;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.StringJoiner;

public class ResourceName {

    private final String packageName;
    private final String type = "id";
    private final String name;

    public ResourceName(final String packageName, final String name) {
        this.packageName = packageName;
        this.name = name;
    }

    public String getFullyQualifiedName() {
        return packageName + ":" + type + "/" + name;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ResourceName that = (ResourceName) o;
        return Objects.equals(packageName, that.packageName) && Objects.equals(type, that.type) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, type, name);
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", ResourceName.class.getSimpleName() + "[", "]")
                .add("packageName='" + packageName + "'")
                .add("type='" + type + "'")
                .add("name='" + name + "'")
                .toString();
    }
}
