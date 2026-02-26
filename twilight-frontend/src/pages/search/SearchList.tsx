import { Card, Container, SimpleGrid, Text } from "@mantine/core";
import { Link } from "react-router-dom";

type Broadcast = {
  channelName: string;
  broadcasterId: string;
  streamId: string;
  streamTitle: string;
  vodId: string | null;
  startedAt: string;
};

interface SearchListProps {
  results: Broadcast[];
}

// could also do { results }: { results: Broadcast[] }
export default function SearchList({ results }: SearchListProps) {
  if (!results || results.length === 0) return <Text>No results</Text>;

  return (
    <Container size={"lg"}>
      <SimpleGrid cols={{ sm: 1, md: 2 }} spacing="md">
        {results.map((result) => (
          <Card key={result.streamId} withBorder>
            <Link to={`/broadcast/${result.streamId}/`}>
              <Text truncate={"end"}>{result.streamTitle}</Text>
            </Link>

            <Text size="sm" c={"dimmed"}>
              Stream ID: {result.streamId}
            </Text>
            <Text size="sm" c={"dimmed"}>
              VOD ID: {result.vodId ?? "n/a"}
            </Text>

            <Text size="xs" ta={"right"}>
              {new Date(result.startedAt).toLocaleString()}
            </Text>
          </Card>
        ))}
      </SimpleGrid>
    </Container>
  );
}
