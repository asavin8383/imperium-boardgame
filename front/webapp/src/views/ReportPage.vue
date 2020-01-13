<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">
        <v-container class="px-2 py-1">
            <v-layout class="mb-3" justify-start row>
                <v-card class="pa-3" style="width: 40%; min-width: 350px">
                    <v-flex class="mb-2 pr-2">
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                ID отчета
                            </v-flex>
                            <v-flex align-self-center md6>
                                {{ mainInfo.id }}
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Название
                            </v-flex>
                            <v-flex align-self-center md6>
                                {{ mainInfo.name }}
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Полное название
                            </v-flex>
                            <v-flex align-self-center md6 class="py-2">
                                {{ mainInfo.full_name }}
                            </v-flex>
                        </v-layout>
                        <v-divider></v-divider>
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Отчетный период
                            </v-flex>
                            <v-flex align-self-center md6>
                                {{ mainInfo.period }}
                            </v-flex>
                        </v-layout>
                    </v-flex>
                </v-card>
            </v-layout>
            <v-layout >
                <v-card class="pa-3" style="width: 100%">
                    <v-card-title>
                        <v-flex md6>
                            <v-text-field
                                    v-model="search"
                                    append-icon="search"
                                    label="Найти"
                                    single-line
                                    hide-details
                                    style="width: 50%"
                            ></v-text-field>
                        </v-flex>
                        <v-spacer></v-spacer>
                        <v-btn class="icon-btn" color="primary" @click="getTableData">
                            <v-icon>refresh</v-icon>
                        </v-btn>
                    </v-card-title>
                    <v-data-table-custom
                            :headers="headers"
                            :items="data"
                            :pagination.sync="pagination"
                            :loading="loadData"
                            :rows-per-page-items="pages"
                            class="mx-4"
                            :search="search"
                    >
                        <template v-slot:headers="props">
                            <tr>
                                <th
                                        v-for="header in props.headers"
                                        :key="header.text"
                                        :class="['column ', header.sortable ? 'sortable' : '', pagination.descending ? 'desc' : 'asc', header.value === pagination.sortBy ? 'active' : '', 'text-xs-' + header.align]"
                                        :style="{width: header.width, align: header.align}"
                                        @click="header.sortable ? changeSort(header.value) : ''"
                                >
                                    {{ header.text }}
                                    <v-icon v-if="header.sortable" small>arrow_upward</v-icon>
                                </th>
                            </tr>
                        </template>
                        <template v-slot:items="props">
                            <tr>
                                <td class="text-xs-left">{{ props.item.msr_prd_tp | noData}}</td>
                                <td class="text-xs-left">{{ props.item.msr_prd_caption | noData}}</td>
                                <td class="text-xs-center">{{ dateTableFormat(props.item.finish_dttm) | noData}}</td>
                                <td class="text-xs-center">
                                    <div style="display: flex; justify-content: center;">
                                        <template v-if="true">
                                            <v-icon class="icon-btn" :color="statusColor(props.item.statuses, 'xlsx')"
                                                    @click="fileStatus(props.item.statuses, 'xlsx') === 'OK' ? openReport(props.item, 'xlsx') : ''">mdi-file-excel</v-icon>
                                            <v-icon class="icon-btn" :color="statusColor(props.item.statuses, 'docx')"
                                                    @click="fileStatus(props.item.statuses, 'docx') === 'OK' ? openReport(props.item, 'docx') : ''">mdi-file-word</v-icon>
                                            <v-icon class="icon-btn" :color="statusColor(props.item.statuses, 'pdf')"
                                                    @click="fileStatus(props.item.statuses, 'pdf') === 'OK' ? openReport(props.item, 'pdf') : ''">mdi-file-pdf</v-icon>
                                        </template>
                                        <template v-else>
                                            <div>ERROR</div>
                                        </template>
                                    </div>
                                </td>
                            </tr>
                        </template>
                        <template v-slot:no-results>
                            <v-alert :value="true" color="warning" icon="warning">
                                По запросу "{{ search }}" ничего не найдено.
                            </v-alert>
                        </template>
                        <template v-slot:no-data v-if="!loadData">
                            <v-alert :value="true" color="warning" icon="warning">
                                Нет данных для отображения.
                            </v-alert>
                        </template>
                    </v-data-table-custom>
                </v-card>
            </v-layout>
        </v-container>
    </v-layout>
</template>

<script>
    import moment from 'moment';
    import * as VueNotifications from "vue-notifications";

    export default {
        name: "ReportPage",
        components: {},

        data() {
            return {
                repId: null,
                mainInfo: {},

                pagination: {
                    sortBy: 'finish_dttm',
                    descending: true
                },
                pages: [10, 25, 50, 100],
                rowsPerPage: null,
                search: '',
                loadData: false,

/*                page: null,
                sortBy: null,
                descending: null,
                total: 0,*/
                params: [],
                headers: [
                    {text: 'Тип периода', value: 'msr_prd_tp', width: '30%', align: 'left', sortable: true},
                    {text: 'Период', value: 'msr_prd_caption', width: '30%', align: 'left', sortable: true},
                    {text: 'Дата и время формирования отчета', value: 'finish_dttm', width: '30%', align: 'center', sortable: true},
                    {text: 'Действия', value: 'actions', width: '10%', align: 'center', sortable: false}
                ],

                data: [],
                //refreshData: false,
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showInfoMsg: {
                type: VueNotifications.types.info,
                title: '',
                message: ''
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.getTableData();
                    this.page = v.page-1;
                    this.rowsPerPage = v.rowsPerPage;
                    this.sortBy = v.sortBy;
                    this.descending = v.descending;
                },
                deep: true
            },

            search() {
                this.pagination.page = 1;
                this.getTableData();
            },

            repId(v) {
                console.log("repId changed");
                this.getMainInfo(v);
                this.getTableData();
            }
        },

        computed: {
            today() {
                return moment().locale('ru').format('hh:mm DD.MM.YYYY');
            }
        },

        mounted() {
            this.repId = this.$route.params.rep_id;
        },

        updated() {
            this.repId = this.$route.params.rep_id;
        },

        methods: {
            getTableData() {
                console.log("getTableData");
                this.params = [];
                this.loadData = true;
                /*const config = () => ({
                    params: {
                        pageSize: pageSize,
                        pageNumber: pageNumber,
                        sortingColumn: sortingColumn,
                        sortingDirection: this.sortTable(sortingDirection)
                    }
                });*/

                const configSearch = () => ({params: {
/*                        pageSize: pageSize,
                        pageNumber: pageNumber,
                        sortingColumn: sortingColumn,
                        sortingDirection: this.sortTable(sortingDirection),*/
                        query: this.search
                    }});

                this.$axios.get(this.$urls.REPORT_TABLE + '/' + this.repId, this.search ? configSearch() : null)
                    .then(resp => {
                        this.data = resp.data;
                        //this.total = resp.data.totalElements;
                        //this.pagination.totalItems = resp.data.totalElements;
                })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() => {
                        this.loadData = false;
                    });
            },

            getMainInfo(id) {
                if (id) {
                    this.$axios.get(this.$urls.REPORT_MAIN_INFO + '/' + id)
                        .then(resp => {
                            this.mainInfo = {
                                id: resp.data.rep_tp_id,
                                name: resp.data.shrt_nm,
                                full_name: resp.data.name,
                                period: resp.data.periods_label
                            };
                        })
                        .catch(e => {
                            console.log('error: ', e);
                        });
                }
                else {
                    this.mainInfo = {
                        id: 0,
                        name: 'Неизвестно',
                        full_name: 'Неизвестно',
                        change_dtm: null
                    }
                }
            },

            changeSort (column) {
                if (this.pagination.sortBy === column) {
                    this.pagination.descending = !this.pagination.descending
                } else {
                    this.pagination.sortBy = column;
                    this.pagination.descending = false
                }
            },

            dateTableFormat(date) {
                if (date == null)
                    return date;
                else {
                    return moment(date).locale('ru').format('DD.MM.YY hh:mm');
                }
            },

            fileStatus(statuses, form) {
                return statuses.find(status => status.format === form).status;
            },

            statusColor(statuses, form) {
                switch(this.fileStatus(statuses, form)) {
                    case 'NEW':
                        return '#1DB340';
                    case 'RUNNING':
                        return '#F5BB47';
                    case 'OK':
                        return 'primary';
                    case 'FAILED':
                        return '#E55949';
                    default:
                        return ''
                }
            },

            openReport(item, form) {
                console.log("open report " + item);
                let rep_id = item.statuses.find(status => status.format === form).rep_id;
                this.$axios.get(this.$urls.GET_REPORT + '/' + rep_id, {responseType: 'arraybuffer'})
                    .then((response) => {
                        const url = window.URL.createObjectURL(new Blob([response.data]));
                        const link = document.createElement('a');
                        link.href = url;
                        const filename = 'Report.' + form;
                        link.setAttribute('download', filename);
                        document.body.appendChild(link);
                        link.click();
                        //link.close();
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    });
            }
        }
    }
</script>

<style scoped>
    .text-style {
        text-align: center;
    }

    .lineText {
        width: 100%;
        min-height: 45px;
    }

</style>
<style>
    .icon-btn {
        padding: 3px !important;
        height: auto !important;
        cursor: pointer;
    }
    .icon-btn.v-btn {
        min-width: 0 !important;
    }
    .icon-btn .v-icon {
        font-size: 30px;
    }
</style>