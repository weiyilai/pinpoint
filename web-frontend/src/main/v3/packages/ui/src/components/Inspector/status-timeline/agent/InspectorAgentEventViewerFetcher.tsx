import { useGetInspectorAgentEvents, useTimezone } from '@pinpoint-fe/ui/src/hooks';
import { formatInTimeZone } from 'date-fns-tz';
import { DataTable } from '../../../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { InspectorAgentEvents } from '@pinpoint-fe/ui/src/constants';

export interface InspectorAgentEventViewerFetcherProps {
  eventRange: number[];
  className?: string;
}

export const InspectorAgentEventViewerFetcher = ({
  eventRange,
  className,
}: InspectorAgentEventViewerFetcherProps) => {
  const { data } = useGetInspectorAgentEvents({ range: eventRange });

  return (
    <div className={className}>
      <DataTable tableClassName="text-xs" columns={columns} data={data || []} />
    </div>
  );
};

const columns: ColumnDef<InspectorAgentEvents.AgentEventData>[] = [
  {
    accessorKey: 'eventTimestamp',
    header: 'Time',
    cell: (props) => {
      const timestamp = props.getValue() as number;
      const [timezone] = useTimezone();

      return formatInTimeZone(timestamp, timezone, 'yyyy.MM.dd HH:mm:ss XXX');
    },
  },
  {
    accessorKey: 'eventTypeDesc',
    header: 'Description',
  },
  {
    accessorKey: 'eventMessage',
    header: 'Message',
  },
];
