/* eslint-disable */

// @ts-nocheck

// noinspection JSUnusedGlobalSymbols

// This file was automatically generated by TanStack Router.
// You should NOT make any changes in this file as it will be overwritten.
// Additionally, you should also exclude this file from your linter and/or formatter to prevent it from being checked or modified.

// Import Routes

import { Route as rootRoute } from './routes/__root'
import { Route as IndexImport } from './routes/index'
import { Route as StationStationIdIndexImport } from './routes/station/$stationId/index'

// Create/Update Routes

const IndexRoute = IndexImport.update({
  id: '/',
  path: '/',
  getParentRoute: () => rootRoute,
} as any)

const StationStationIdIndexRoute = StationStationIdIndexImport.update({
  id: '/station/$stationId/',
  path: '/station/$stationId/',
  getParentRoute: () => rootRoute,
} as any)

// Populate the FileRoutesByPath interface

declare module '@tanstack/react-router' {
  interface FileRoutesByPath {
    '/': {
      id: '/'
      path: '/'
      fullPath: '/'
      preLoaderRoute: typeof IndexImport
      parentRoute: typeof rootRoute
    }
    '/station/$stationId/': {
      id: '/station/$stationId/'
      path: '/station/$stationId'
      fullPath: '/station/$stationId'
      preLoaderRoute: typeof StationStationIdIndexImport
      parentRoute: typeof rootRoute
    }
  }
}

// Create and export the route tree

export interface FileRoutesByFullPath {
  '/': typeof IndexRoute
  '/station/$stationId': typeof StationStationIdIndexRoute
}

export interface FileRoutesByTo {
  '/': typeof IndexRoute
  '/station/$stationId': typeof StationStationIdIndexRoute
}

export interface FileRoutesById {
  __root__: typeof rootRoute
  '/': typeof IndexRoute
  '/station/$stationId/': typeof StationStationIdIndexRoute
}

export interface FileRouteTypes {
  fileRoutesByFullPath: FileRoutesByFullPath
  fullPaths: '/' | '/station/$stationId'
  fileRoutesByTo: FileRoutesByTo
  to: '/' | '/station/$stationId'
  id: '__root__' | '/' | '/station/$stationId/'
  fileRoutesById: FileRoutesById
}

export interface RootRouteChildren {
  IndexRoute: typeof IndexRoute
  StationStationIdIndexRoute: typeof StationStationIdIndexRoute
}

const rootRouteChildren: RootRouteChildren = {
  IndexRoute: IndexRoute,
  StationStationIdIndexRoute: StationStationIdIndexRoute,
}

export const routeTree = rootRoute
  ._addFileChildren(rootRouteChildren)
  ._addFileTypes<FileRouteTypes>()

/* ROUTE_MANIFEST_START
{
  "routes": {
    "__root__": {
      "filePath": "__root.tsx",
      "children": [
        "/",
        "/station/$stationId/"
      ]
    },
    "/": {
      "filePath": "index.tsx"
    },
    "/station/$stationId/": {
      "filePath": "station/$stationId/index.tsx"
    }
  }
}
ROUTE_MANIFEST_END */
