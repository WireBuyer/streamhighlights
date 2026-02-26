import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Text,
  Stack,
  Card,
  Grid,
  Box,
  Select,
  Loader,
  Group,
} from "@mantine/core";

type Broadcast = {
  channelName: string;
  broadcasterId: string;
  streamId: string;
  streamTitle: string;
  vodId: string | null;
  startedAt: number;
};

type Spike = {
  sensitivity: string;
  spikeStart: number;
  spikeEnd: number;
};

export default function BroadcastPage() {
  const { streamId } = useParams();

  const [broadcast, setBroadcast] = useState<Broadcast | null>(null);
  const [spikes, setSpikes] = useState<Spike[]>([]);
  const [loadingBroadcast, setLoadingBroadcast] = useState(false);
  const [loadingSpikes, setLoadingSpikes] = useState(false);
  const [sensitivity, setSensitivity] = useState<string>("LOW");

  useEffect(() => {
    if (!streamId) return;
    const fetchBroadcast = async () => {
      setLoadingBroadcast(true);
      try {
        const res = await fetch(
          `/api/broadcasts/${encodeURIComponent(streamId)}`,
        );
        if (!res.ok) throw new Error("Failed to fetch broadcast");
        const data = await res.json();
        setBroadcast(data);
      } catch (e) {
        console.error(e);
        setBroadcast(null);
      } finally {
        setLoadingBroadcast(false);
      }
    };

    fetchBroadcast();
  }, [streamId]);

  useEffect(() => {
    if (!streamId) return;
    const fetchSpikes = async () => {
      setLoadingSpikes(true);
      try {
        const url = `/api/broadcasts/${encodeURIComponent(streamId)}/spikes?sensitivity=${encodeURIComponent(
          sensitivity,
        )}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error("Failed to fetch spikes");
        const data = await res.json();
        setSpikes(data ?? []);
      } catch (e) {
        console.error(e);
        setSpikes([]);
      } finally {
        setLoadingSpikes(false);
      }
    };

    fetchSpikes();
  }, [streamId, sensitivity]);

  const formatOffset = (ms: number) => {
    const totalSec = Math.max(0, Math.floor(ms / 1000));
    const h = Math.floor(totalSec / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    if (h > 0)
      return `${h}:${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
    return `${m}:${s.toString().padStart(2, "0")}`;
  };

  return (
    <Box p={16}>
      <Grid>
        {/* metadata portion */}
        <Grid.Col span={4}>
          <Stack>
            {/* title */}
            <Text fw={700} size="lg">
              Broadcast
            </Text>

            {/* metadata */}
            <Card withBorder p="md">
              {loadingBroadcast ? (
                <Loader />
              ) : (
                <Stack style={{ gap: 6 }}>
                  <Text size="sm">
                    Title: {broadcast?.streamTitle ?? "n/a"}
                  </Text>
                  <Text size="xs" c={"dimmed"}>
                    VOD: {broadcast?.vodId ?? "n/a"}
                  </Text>
                  <Text size="xs" c={"dimmed"}>
                    Stream ID: {broadcast?.streamId ?? "n/a"}
                  </Text>
                  <Text size="xs" c={"dimmed"}>
                    Channel: {broadcast?.channelName ?? "n/a"}
                  </Text>
                  <Text size="xs" c={"dimmed"}>
                    Started:{" "}
                    {broadcast?.startedAt
                      ? new Date(Number(broadcast.startedAt)).toLocaleString()
                      : "n/a"}
                  </Text>
                </Stack>
              )}
            </Card>

            {/* sensitivity */}
            <Card withBorder p="md">
              <Text fw={600} size="sm" style={{ marginBottom: 8 }}>
                Sensitivity
              </Text>
              <Select
                value={sensitivity}
                onChange={(v) => setSensitivity(v || "LOW")}
                data={[
                  { value: "LOW", label: "Low" },
                  { value: "MEDIUM", label: "Medium" },
                  { value: "HIGH", label: "High" },
                ]}
              />
            </Card>
          </Stack>
        </Grid.Col>

        {/* spikes portion */}
        <Grid.Col span={8}>
          <Stack>
            <Group>
              <Text fw={700} size="lg">
                Spikes
              </Text>
              <Text size="sm" c={"dimmed"}>
                {loadingSpikes ? "Loading..." : `${spikes.length} items`}
              </Text>
            </Group>

            <Card withBorder>
              <Box
                style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(auto-fill, 130px)",
                  gap: 8,
                  padding: 8,
                }}
              >
                {spikes.length === 0 && !loadingSpikes && (
                  <Text color="dimmed">
                    No spikes found for this broadcast.
                  </Text>
                )}

                {spikes.map((s, idx) => (
                  <Card
                    key={idx}
                    withBorder
                    p="xs"
                    style={{
                      alignItems: "center",
                    }}
                  >
                    <Text size="xs">
                      {broadcast
                        ? `${formatOffset(Number(s.spikeStart - broadcast.startedAt))} - ${formatOffset(
                            Number(s.spikeEnd - broadcast.startedAt),
                          )}`
                        : "n/a"}
                    </Text>
                  </Card>
                ))}
              </Box>
            </Card>
          </Stack>
        </Grid.Col>
      </Grid>
    </Box>
  );
}
