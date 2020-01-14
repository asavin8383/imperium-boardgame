<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-start justify-start fill-height>
        <v-card class="main-card pb-4 ma-2" style="font-size: 12px" width="30%">
            <v-card-title class="font-weight-bold">
                {{reglamentReports.title}}
            </v-card-title>
            <v-card-text class="pa-0">
                <v-list v-for="item in reglamentReports.stats" :key="item.id" class="pa-0">
                    <v-list-tile @click="showReport(item.rep_tp_id)">
                        <v-list-tile-content>
                            <v-list-tile-title class="label-font">
                                {{ item.label }}
                            </v-list-tile-title>
                        </v-list-tile-content>
                        <v-list-tile-action style="color:blue" class="num-font">
                            {{ item.cnt }}
                        </v-list-tile-action>
                    </v-list-tile>
                    <v-divider></v-divider>
                </v-list>
            </v-card-text>
        </v-card>
        <v-card class="main-card pb-4 ma-2" style="font-size: 12px" width="30%">
            <v-card-title class="font-weight-bold">
                {{paramReports.title}}
            </v-card-title>
            <v-card-text class="pa-0">
                <v-list v-for="item in paramReports.stats" :key="item.id" class="pa-0">
                    <v-list-tile @click="showParamReport(item.rep_tp_id)">
                        <v-list-tile-content>
                            <v-list-tile-title class="label-font">
                                {{ item.label }}
                            </v-list-tile-title>
                        </v-list-tile-content>
                        <v-list-tile-action style="color:blue" class="num-font">
                            {{ item.cnt }}
                        </v-list-tile-action>
                    </v-list-tile>
                    <v-divider></v-divider>
                </v-list>
            </v-card-text>
        </v-card>
    </v-layout>
</template>

<script>
    export default {
        name: "ReportList",

        data() {
            return {
                reglamentReports: {
                    title: 'Отчеты',
                    stats: []
                },

                paramReports: {
                    title: 'Параметризированные отчеты',
                    stats: []
                },

            }
        },

        mounted() {
            this.$axios.get(this.$urls.REPORTS_STAT)
                .then(resp => { this.reglamentReports.stats = resp.data; })
                .catch((e) => { console.log('error', e);});

            this.$axios.get(this.$urls.PARAM_REPORTS_STAT)
                .then(resp => { this.paramReports.stats = resp.data; })
                .catch((e) => { console.log('error', e);});
        },

        methods: {
            showReport(id) {
                this.$router.push({name: 'report', params: {rep_id: id}});
            },

            showParamReport(id) {
                this.$router.push({name: 'param-report', params: {rep_id: id}});
            },
        }
    }
</script>

<style scoped>
    .main-card {
        border-radius: 15px;
        font-size: 12px;
        max-width: 350px;
    }

    .num-font {
        color: blue;
        font-size: 12px;
    }

    .label-font {
        font-size: 12px;
    }
</style>