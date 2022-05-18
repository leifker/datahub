package datahub.integration.visitors;


import com.linkedin.common.InstitutionalMemoryMetadata;
import com.linkedin.common.url.Url;
import datahub.integration.SchemaContext;
import datahub.integration.SchemaVisitor;
import datahub.integration.model.FieldNode;
import datahub.integration.model.Node;
import datahub.integration.model.SchemaEdge;
import datahub.integration.model.SchemaGraph;
import datahub.integration.model.SchemaNode;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InstitutionalMemoryVisitor<G extends SchemaGraph<N, S, F, E>, C extends SchemaContext<G, C, N, S, F, E>,
        N extends Node<N, S, F, E>, S extends SchemaNode<N, S, F, E>, F extends FieldNode<N, S, F, E>,
        E extends SchemaEdge<N, S, F, E>>  implements SchemaVisitor<InstitutionalMemoryMetadata, G, C, N, S, F, E> {
    public static final String TEAM_DESC =  "Github Team";
    public static final String SLACK_CHAN_DESC = "Slack Channel";

    protected static final Pattern SLACK_CHANNEL_REGEX = Pattern.compile("(?si).*#([a-z0-9-]+).*");
    protected static final Pattern LINK_REGEX = Pattern.compile("(?s)(\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");
    private final String githubOrganization;
    private final Pattern githubTeamRegex;
    private final String slackTeamId;

    public InstitutionalMemoryVisitor(@Nullable String slackTeamId, @Nullable String githubOrganization) {
        this.slackTeamId = slackTeamId;
        this.githubOrganization = githubOrganization;
        if (githubOrganization != null) {
            this.githubTeamRegex = Pattern.compile(String.format("(?si).*@%s/([a-z-]+).*", githubOrganization));
        } else {
            this.githubTeamRegex = null;
        }
    }

    @Override
    public Stream<InstitutionalMemoryMetadata> visitGraph(C context) {
        List<InstitutionalMemoryMetadata> institutionalMemoryMetadata = new LinkedList<>();

        teamLink(context.root().description()).ifPresent(url ->
                institutionalMemoryMetadata.add(new InstitutionalMemoryMetadata()
                        .setCreateStamp(context.getAuditStamp())
                        .setDescription(TEAM_DESC)
                        .setUrl(url)));


        slackLink(context.root().description()).ifPresent(url ->
                institutionalMemoryMetadata.add(new InstitutionalMemoryMetadata()
                        .setCreateStamp(context.getAuditStamp())
                        .setDescription(SLACK_CHAN_DESC)
                        .setUrl(url)));

        final int[] cnt = {0};
        MatcherStream.findMatches(LINK_REGEX, context.root().description()).forEach(match -> {
            cnt[0] += 1;
            institutionalMemoryMetadata.add(new InstitutionalMemoryMetadata()
                    .setCreateStamp(context.getAuditStamp())
                    .setDescription(String.format("%s Reference %d", StringUtils.capitalize(context.root().name()), cnt[0]))
                    .setUrl(new Url(match.group(1))));
        });

        return institutionalMemoryMetadata.stream();
    }

    @Override
    public Stream<InstitutionalMemoryMetadata> visitField(F field, C context) {
        List<InstitutionalMemoryMetadata> institutionalMemoryMetadata = new LinkedList<>();

        if (field.parentSchema().equals(context.graph().root())) {
            final int[] cnt = {0};
            MatcherStream.findMatches(LINK_REGEX, field.description()).forEach(match -> {
                cnt[0] += 1;
                String[] parentName = field.parentSchemaName().split("[.]");
                institutionalMemoryMetadata.add(new InstitutionalMemoryMetadata()
                        .setCreateStamp(context.getAuditStamp())
                        .setDescription(String.format("%s.%s Reference %d",
                                        StringUtils.capitalize(parentName[parentName.length - 1]),
                                field.name(),
                                cnt[0]))
                        .setUrl(new Url(match.group(1))));
            });
        }

        return institutionalMemoryMetadata.stream();
    }

    //  https://slack.com/app_redirect?channel=fdn-analytics-data-catalog&team=T024F4EL1
    protected Optional<Url> slackLink(String text) {
        return Optional.ofNullable(slackTeamId).map(teamId -> {
            Matcher m = SLACK_CHANNEL_REGEX.matcher(text);
            if (m.matches()) {
                return new Url(String.format("https://slack.com/app_redirect?channel=%s&team=%s", m.group(1), slackTeamId));
            } else {
                return null;
            }
        });
    }

    protected Optional<Url> teamLink(String text) {
        return Optional.ofNullable(githubTeamRegex).map(regex -> {
            Matcher m = regex.matcher(text);
            if (m.matches()) {
                return new Url(String.format("https://github.com/orgs/%s/teams/%s", githubOrganization, m.group(1)));
            } else {
                return null;
            }
        });
    }

    private static class MatcherStream {
        private MatcherStream() { }

        public static Stream<String> find(Pattern pattern, CharSequence input) {
            return findMatches(pattern, input).map(MatchResult::group);
        }

        public static Stream<MatchResult> findMatches(
                Pattern pattern, CharSequence input) {
            Matcher matcher = pattern.matcher(input);

            Spliterator<MatchResult> spliterator = new Spliterators.AbstractSpliterator<MatchResult>(
                    Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
                @Override
                public boolean tryAdvance(Consumer<? super MatchResult> action) {
                    if (!matcher.find()) {
                        return false;
                    }
                    action.accept(matcher.toMatchResult());
                    return true;
                } };

            return StreamSupport.stream(spliterator, false);
        }
    }
}
