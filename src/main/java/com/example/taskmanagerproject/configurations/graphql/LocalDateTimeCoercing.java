package com.example.taskmanagerproject.configurations.graphql;

import static java.time.LocalDateTime.parse;
import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;
import static java.util.Locale.ENGLISH;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Coercing implementation for LocalDateTime scalar type.
 */
public class LocalDateTimeCoercing implements Coercing<LocalDateTime, String> {

  @Override
  public @Nullable String serialize(
      @NotNull final Object dataFetcherResult,
      @NotNull final GraphQLContext graphQlContext,
      @NotNull final Locale locale
  ) throws CoercingSerializeException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", ENGLISH);
    return formatter.format(
      from(((LocalDateTime) dataFetcherResult)
        .atZone(systemDefault())
        .toInstant())
    );
  }

  @Override
  public @Nullable LocalDateTime parseValue(
      @NotNull final Object input,
      @NotNull final GraphQLContext graphQlContext,
      @NotNull final Locale locale
  ) throws CoercingParseValueException {
    return parse((String) input);
  }

  @Override
  public @Nullable LocalDateTime parseLiteral(
      @NotNull final Value<?> input,
      @NotNull final CoercedVariables variables,
      @NotNull final GraphQLContext graphQlContext,
      @NotNull final Locale locale
  ) throws CoercingParseLiteralException {
    return parse(((StringValue) input).getValue());
  }
}
