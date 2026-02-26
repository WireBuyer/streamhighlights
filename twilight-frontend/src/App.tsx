import { AppShell, Group, useMantineTheme, Text } from "@mantine/core";
import { lazy, Suspense } from "react";
import { Routes, Route, Link } from "react-router-dom";
import SearchPage from "./pages/search/SearchPage";

const BroadcastPage = lazy(() => import("./pages/broadcast/BroadcastPage"));

export default function App() {
  const theme = useMantineTheme();

  return (
    <AppShell
      header={{ height: 60 }}
      style={{
        minWidth: theme.breakpoints.xs,
      }}
    >
      <AppShell.Header style={{ minWidth: theme.breakpoints.xs }}>
        <Group justify="space-between" h="100%" px="md">
          <Text fw={10}>Stream Highlights</Text>
          <Group>
            <Link to="/">Home</Link>
            <Link to="/long">Long page</Link>
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Main pt={"calc(60px + 2rem)"}>
        <Suspense>
          <Routes>
            <Route path="/" element={<SearchPage />} />
            <Route path="/broadcast/:streamId/" element={<BroadcastPage />} />
          </Routes>
        </Suspense>
      </AppShell.Main>
    </AppShell>
  );
}
