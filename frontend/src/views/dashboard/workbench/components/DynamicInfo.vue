<template>
  <Card title="Hoạt động gần đây" v-bind="$attrs">
    <template #extra>
      <Button type="link" size="small">Xem thêm</Button>
    </template>
    <List item-layout="horizontal" :data-source="dynamicInfoItems">
      <template #renderItem="{ item }">
        <ListItem>
          <ListItemMeta>
            <template #description>
              {{ item.date }}
            </template>
            <!-- eslint-disable-next-line -->
            <template #title> {{ item.name }} <span v-html="item.desc"> </span> </template>
            <template #avatar>
              <Icon :icon="item.avatar" :size="30" />
            </template>
          </ListItemMeta>
        </ListItem>
      </template>
    </List>
  </Card>
</template>
<script lang="ts" setup>
  import { Button } from '@/components/Button';
  import { Card, List } from 'ant-design-vue';
  import { dynamicInfoItems } from './data';
  import Icon from '@/components/Icon/Icon.vue';

  const ListItem = List.Item;
  const ListItemMeta = List.Item.Meta;
</script>
