import { useState, useRef } from "react";
import {
  Center,
  Stack,
  Group,
  TextInput,
  Button,
  Text,
  Box,
} from "@mantine/core";
import SearchList from "./SearchList";

type Broadcast = {
  channelName: string;
  broadcasterId: string;
  streamId: string;
  streamTitle: string;
  vodId: string | null;
  startedAt: string;
};

const dummyData: Broadcast[] = [
  {
    channelName: "dummy",
    broadcasterId: "dummy",
    streamId: "dummy",
    streamTitle: "dummy",
    vodId: null,
    startedAt: "dummy",
  },
];

export default function SearchPage() {
  const queryRef = useRef<string>("");
  const [results, setResults] = useState<Broadcast[]>(dummyData);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchPage = async (page: number) => {
    try {
      const channelName = queryRef.current;
      const url = `/api/broadcasts?channelName=${encodeURIComponent(channelName)}&page=${page}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error("could not get data. Status: " + response.status);
      }
      const data = await response.json();

      setResults(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error("Error fetching data:", error);
    }

    setPage(page);
  };

  const onSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const q = (data.get("channelQuery") as string).trim();
    queryRef.current = q;

    setPage(0);
    fetchPage(0);
  };

  return (
    <>
      <Center w={"100%"} mt={0}>
        <Stack style={{ width: 640 }} m={16}>
          <form onSubmit={onSubmit}>
            <Group>
              <TextInput
                name="channelQuery"
                placeholder="Search channel..."
                flex={1}
              />
              <Button type="submit">Search</Button>
            </Group>
          </form>
        </Stack>
      </Center>

      <Box>
        <SearchList results={results} />

        <Center>
          <Group mt={16}>
            <Button
              disabled={page <= 0}
              onClick={() => fetchPage(Math.max(0, page - 1))}
            >
              Prev
            </Button>
            <Text>
              Page {page + 1} / {totalPages}
            </Text>
            <Button
              disabled={page + 1 >= totalPages}
              onClick={() => fetchPage(page + 1)}
            >
              Next
            </Button>
          </Group>
        </Center>
      </Box>
    </>
  );
}
