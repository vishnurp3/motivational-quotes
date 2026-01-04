package com.vishnu.quote.infrastructure.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClasspathQuoteRepository")
final class ClasspathQuoteRepositoryTest {

    @Nested
    @DisplayName("constructor")
    final class Constructor {

        @Test
        void should_throwNullPointerException_when_resourceNameIsNull() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> new ClasspathQuoteRepository(null)
            );
            assertEquals("resourceName", ex.getMessage());
        }

        @Test
        void should_throwIllegalArgumentException_when_resourceNameIsBlank() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ClasspathQuoteRepository("   ")
            );
            assertEquals("resourceName must not be blank", ex.getMessage());
        }

        @Test
        void should_throwIllegalArgumentException_when_resourceNameStartsWithSlash() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ClasspathQuoteRepository("/quotes.txt")
            );
            assertEquals("resourceName must not start with '/': /quotes.txt", ex.getMessage());
        }

        @Test
        void should_trimResourceName() {
            ClasspathQuoteRepository repo = new ClasspathQuoteRepository("  quotes/motivational.txt  ");
            assertEquals("classpath:quotes/motivational.txt", repo.description());
        }
    }

    @Nested
    @DisplayName("description()")
    final class Description {

        @Test
        void should_prefixClasspath_andUseValidatedResourceName() {
            ClasspathQuoteRepository repo = new ClasspathQuoteRepository("  q.txt ");
            assertEquals("classpath:q.txt", repo.description());
        }
    }

    @Nested
    @DisplayName("randomQuote()")
    final class RandomQuote {

        @Test
        void should_returnEmpty_when_resourceDoesNotExist() {
            withContextClassLoader(new InMemoryClassLoader(), () -> {
                ClasspathQuoteRepository repo = new ClasspathQuoteRepository("missing.txt");

                Optional<String> result = repo.randomQuote();

                assertTrue(result.isEmpty());
            });
        }

        @Test
        void should_returnEmpty_when_resourceLoadsToNoQuotes_afterFiltering() {
            String content = """
                    # comment line
                    
                       # another comment with leading spaces
                    
                    \t
                    """;

            withContextClassLoader(new InMemoryClassLoader().withResource("quotes.txt", content), () -> {
                ClasspathQuoteRepository repo = new ClasspathQuoteRepository("quotes.txt");

                Optional<String> result = repo.randomQuote();

                assertTrue(result.isEmpty());
            });
        }

        @Test
        void should_returnThatOnlyQuote_when_exactlyOneQuoteExists() {
            withContextClassLoader(new InMemoryClassLoader().withResource("one.txt", "  Keep going.  \n"), () -> {
                ClasspathQuoteRepository repo = new ClasspathQuoteRepository("one.txt");

                Optional<String> result = repo.randomQuote();

                assertTrue(result.isPresent());
                assertEquals("Keep going.", result.get());
            });
        }

        @Test
        void should_returnOneOfLoadedQuotes_andNeverReturnBlankOrCommentLines() {
            String content = """
                    # ignore this
                    
                      Small steps every day.
                    \t
                    # also ignore
                    Discipline beats motivation.
                      Focus on the next action.
                    """;

            Set<String> expected = Set.of(
                    "Small steps every day.",
                    "Discipline beats motivation.",
                    "Focus on the next action."
            );

            withContextClassLoader(new InMemoryClassLoader().withResource("quotes.txt", content), () -> {
                ClasspathQuoteRepository repo = new ClasspathQuoteRepository("quotes.txt");

                Optional<String> result = repo.randomQuote();

                assertTrue(result.isPresent());
                assertTrue(expected.contains(result.get()), "Returned quote must be one of the filtered lines.");
                assertFalse(result.get().isBlank(), "Returned quote must not be blank.");
                assertFalse(result.get().startsWith("#"), "Returned quote must not be a comment.");
                assertEquals(result.get().trim(), result.get(), "Returned quote must be trimmed.");
            });
        }

        @Test
        void should_cacheLoadedQuotes_andNotReloadOnSubsequentCalls() {
            String content = """
                    Quote A
                    Quote B
                    """;

            CountingInMemoryClassLoader cl = new CountingInMemoryClassLoader()
                    .withResource("quotes.txt", content);

            withContextClassLoader(cl, () -> {
                ClasspathQuoteRepository repo = new ClasspathQuoteRepository("quotes.txt");

                Optional<String> first = repo.randomQuote();
                Optional<String> second = repo.randomQuote();
                Optional<String> third = repo.randomQuote();

                assertTrue(first.isPresent());
                assertTrue(second.isPresent());
                assertTrue(third.isPresent());

                assertEquals(1, cl.openCount());
            });
        }
    }

    private static void withContextClassLoader(ClassLoader cl, Runnable action) {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            action.run();
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private static class InMemoryClassLoader extends ClassLoader {

        protected final java.util.Map<String, byte[]> resources = new java.util.HashMap<>();

        InMemoryClassLoader() {
            super(ClasspathQuoteRepositoryTest.class.getClassLoader());
        }

        InMemoryClassLoader withResource(String name, String contentUtf8) {
            resources.put(name, contentUtf8.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            byte[] bytes = resources.get(name);
            if (bytes != null) {
                return new ByteArrayInputStream(bytes);
            }
            return super.getResourceAsStream(name);
        }
    }

    private static final class CountingInMemoryClassLoader extends InMemoryClassLoader {

        private final java.util.Map<String, AtomicInteger> counts = new java.util.HashMap<>();

        @Override
        CountingInMemoryClassLoader withResource(String name, String contentUtf8) {
            super.withResource(name, contentUtf8);
            return this;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            counts.computeIfAbsent(name, k -> new AtomicInteger()).incrementAndGet();
            return super.getResourceAsStream(name);
        }

        int openCount() {
            AtomicInteger c = counts.get("quotes.txt");
            return c == null ? 0 : c.get();
        }
    }
}
