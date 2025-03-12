package msaspring.apigateway

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig {

    @Bean
    fun longScalar(): GraphQLScalarType {
        return GraphQLScalarType.newScalar()
            .name("Long")
            .description("A custom scalar that handles 64-bit integers")
            .coercing(object : Coercing<Long, Long> {

                override fun serialize(dataFetcherResult: Any): Long {
                    return try {
                        if (dataFetcherResult is String) {
                            dataFetcherResult.toLong()
                        } else {
                            (dataFetcherResult as Number).toLong()
                        }
                    } catch (e: Exception) {
                        throw CoercingSerializeException("Unable to serialize $dataFetcherResult as a Long", e)
                    }
                }

                override fun parseValue(input: Any): Long {
                    return try {
                        when (input) {
                            is String -> input.toLong()
                            is Number -> input.toLong()
                            else -> throw IllegalArgumentException("Unexpected type: ${input::class.simpleName}")
                        }
                    } catch (e: Exception) {
                        throw CoercingParseLiteralException("Unable to parse variable value $input as a Long", e)
                    }
                }

                override fun parseLiteral(input: Any): Long {
                    return when (input) {
                        is IntValue -> input.value.toLong()
                        is StringValue -> input.value.toLong()
                        else -> throw CoercingParseLiteralException(
                            "Expected AST type 'IntValue' or 'StringValue' but was: ${input::class.simpleName}"
                        )
                    }
                }
            })
            .build()
    }

    @Bean
    fun graphQlSourceBuilderCustomizer(longScalar: GraphQLScalarType): GraphQlSourceBuilderCustomizer {
        return GraphQlSourceBuilderCustomizer { builder ->
            builder.configureRuntimeWiring { wiringBuilder ->
                wiringBuilder.scalar(longScalar)
            }
        }
    }
}