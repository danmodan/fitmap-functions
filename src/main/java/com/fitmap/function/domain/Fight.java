package com.fitmap.function.domain;

import java.util.List;
import java.util.Locale;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import org.apache.commons.collections4.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(value = Include.NON_ABSENT)
public class Fight {

    public static final String FIGHTS_COLLECTION = "fights_v2";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LANGUAGES = "languages";

    @NotBlank
    private String id;

    @NotBlank
    @Size(max = 1000)
    private String name;

    @NotEmpty
    private List<@NotBlank @Size(max = 2) String> languages;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Fight other = (Fight) obj;
        if (id == null) {
            return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public boolean isLaguageSupported(String lang) {

        if(CollectionUtils.isEmpty(languages)) {
            return false;
        }

        return languages.contains(lang);
    }

    public boolean isLaguageSupported(Locale locale) {

        if(locale == null) {
            return false;
        }

        return isLaguageSupported(locale.getLanguage());
    }

}