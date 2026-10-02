import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
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

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const channelName = searchParams.get("channel") ?? "";
  const pageNumber = Number(searchParams.get("page") ?? 0);
  const page = Number.isInteger(pageNumber) && pageNumber >= 0 ? pageNumber : 0;
  const [results, setResults] = useState<Broadcast[]>([]);
  const [displayedPage, setDisplayedPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    if (!channelName) return;
    const controller = new AbortController();

    const fetchPage = async () => {
      try {
        const url = `/api/broadcasts?channelName=${encodeURIComponent(channelName)}&page=${page}`;
        const response = await fetch(url, { signal: controller.signal });
        if (!response.ok) {
          throw new Error("could not get data. Status: " + response.status);
        }
        const data = await response.json();
        if (controller.signal.aborted) return;

        setResults(data.content);
        setDisplayedPage(page);
        setTotalPages(data.totalPages);
      } catch (error) {
        if (!controller.signal.aborted) {
          console.error("Error fetching data:", error);
        }
      }
    };

    void fetchPage();
    return () => controller.abort();
  }, [channelName, page]);

  const onSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const q = (data.get("channelQuery") as string).trim();
    setSearchParams(q ? { channel: q, page: "0" } : {});
  };

  return (
    <>
      <Center w={"100%"} mt={0}>
        <Stack style={{ width: 640 }} m={16}>
          <form onSubmit={onSubmit}>
            <Group>
              <TextInput
                key={channelName}
                name="channelQuery"
                defaultValue={channelName}
                placeholder="Search channel..."
                flex={1}
              />
              <Button type="submit">Search</Button>
            </Group>
          </form>
        </Stack>
      </Center>

      <Box>
        <SearchList results={channelName ? results : []} />

        <Center>
          <Group mt={16}>
            <Button
              disabled={page <= 0}
              onClick={() => setSearchParams({ channel: channelName, page: String(page - 1) })}
            >
              Prev
            </Button>
            <Text>
              Page {channelName ? displayedPage + 1 : 1} / {channelName ? totalPages : 1}
            </Text>
            <Button
              disabled={page + 1 >= (channelName ? totalPages : 1)}
              onClick={() => setSearchParams({ channel: channelName, page: String(page + 1) })}
            >
              Next
            </Button>
          </Group>
        </Center>
      </Box>
    </>
  );
}
