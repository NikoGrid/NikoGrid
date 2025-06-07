import { $api } from "@/api/client";
import { mediaQueryHelpers, useMediaQuery } from "@/hooks/use-media-query";
import type { Dispatch, SetStateAction } from "react";
import { ChargerCard } from "./charger-card";
import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
} from "./ui/drawer";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "./ui/sheet";

interface StationDetailsProps {
  stationId: number | null;
  setStationId: Dispatch<SetStateAction<number | null>>;
  container?: React.ComponentProps<typeof SheetContent>["portalContainer"];
}

function getWrapper(isDesktop: boolean) {
  return isDesktop
    ? {
        Wrapper: (props: Omit<React.ComponentProps<typeof Sheet>, "modal">) => {
          return <Sheet modal={false} {...props} />;
        },
        Content: (
          props: Omit<
            React.ComponentProps<typeof SheetContent>,
            "side" | "overlay" | "onInteractOutside"
          >,
        ) => {
          return (
            <SheetContent
              side="left"
              overlay={false}
              onInteractOutside={(e) => e.preventDefault()}
              {...props}
            />
          );
        },
        Header: SheetHeader,
        Title: SheetTitle,
        Description: SheetDescription,
      }
    : {
        Wrapper: Drawer,
        Content: DrawerContent,
        Header: DrawerHeader,
        Title: DrawerTitle,
        Description: DrawerDescription,
      };
}

interface DetailsProps {
  stationId: number;
  wrapper: ReturnType<typeof getWrapper>;
}

function Details({ stationId, wrapper }: DetailsProps) {
  const { data, isSuccess } = $api.useQuery("get", "/api/v1/locations/{id}", {
    params: { path: { id: stationId } },
  });

  if (!isSuccess) return null;

  return (
    <>
      <wrapper.Header>
        <wrapper.Title>{data.name}</wrapper.Title>
      </wrapper.Header>
      <div className="space-y-2 overflow-y-auto px-4">
        <h1 className="font-semibold">Chargers</h1>
        {data.chargers.length == 0 && <p>No chargers at this location</p>}
        {data.chargers.map((charger) => (
          <ChargerCard charger={charger} />
        ))}
      </div>
    </>
  );
}

export default function StationDetails({
  stationId,
  setStationId,
  container,
}: StationDetailsProps) {
  const isDesktop = useMediaQuery(mediaQueryHelpers.minWidth("48rem"));
  const Wrapper = getWrapper(isDesktop);

  return (
    <Wrapper.Wrapper
      open={stationId !== null}
      onOpenChange={(value) => {
        if (!value) {
          setStationId(null);
        }
      }}
    >
      <Wrapper.Content portalContainer={container} className="h-3/4 md:h-auto">
        {stationId !== null && (
          <Details stationId={stationId} wrapper={Wrapper} />
        )}
      </Wrapper.Content>
    </Wrapper.Wrapper>
  );
}
