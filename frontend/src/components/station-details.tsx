import { $api } from "@/api/client";
import { mediaQueryHelpers, useMediaQuery } from "@/hooks/use-media-query";
import { cn } from "@/lib/utils";
import { Link } from "@tanstack/react-router";
import type { Dispatch, SetStateAction } from "react";
import { buttonVariants } from "./ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "./ui/dialog";
import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
} from "./ui/drawer";

interface StationDetailsProps {
  stationId: number | null;
  setStationId: Dispatch<SetStateAction<number | null>>;
}

function getWrapper(isDesktop: boolean) {
  return isDesktop
    ? {
        Wrapper: Dialog,
        Content: DialogContent,
        Header: DialogHeader,
        Title: DialogTitle,
        Description: DialogDescription,
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
      <div className="space-y-4 px-4">
        <Link
          to="/station/$stationId"
          params={{ stationId: data.id }}
          className={cn(buttonVariants(), "w-full")}
        >
          Click for details
        </Link>
      </div>
    </>
  );
}

export default function StationDetails({
  stationId,
  setStationId,
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
      <Wrapper.Content className="h-3/4 md:h-auto">
        {stationId !== null && (
          <Details stationId={stationId} wrapper={Wrapper} />
        )}
      </Wrapper.Content>
    </Wrapper.Wrapper>
  );
}
