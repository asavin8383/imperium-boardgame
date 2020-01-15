<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-data-table-custom
			:headers="headers"
			:items.sync="data"
			:search="search"
			:loading="load"
			:total-items="total"
			:pagination.sync="pagination"
			:rows-per-page-items="pages"
			class="mx-4"
			v-model="selected"
			:select-all="!hideCheckBox"
	>
		<template v-slot:items="props">
			<tr>
				<td v-if="!hideCheckBox">
					<v-checkbox
							v-model="props.selected"
							primary
							hide-details
					></v-checkbox>
				</td>
				<td>{{props.item.id}}</td>
				<td>{{props.item.phrase}}</td>
				<td v-if="showActionButtons">
					<div style="display: flex; justify-content: space-between; padding-right: 22%;">
						<v-icon class="icon-btn" color="primary" @click="$emit('actionWithItem', {type: 'edit', item: props.item})">edit</v-icon>
						<v-icon class="icon-btn" color="primary" @click="$emit('actionWithItem', {type: 'delete', item: props.item})">delete</v-icon>
					</div>
				</td>
			</tr>
		</template>
		<template v-slot:no-data v-if="!load">
			<v-alert :value="true" color="warning" icon="warning">
				Нет данных для отображения.
			</v-alert>
		</template>
	</v-data-table-custom>
</template>

<script>
    export default {
        name: "DictionaryTable",

        props: ['isHave', 'id', 'search', 'hideCheckBox', 'showActionButtons'],

        data() {
            return {
                headers:  [
                    {text: 'ID', align: 'left', value: 'id'},
                    {text: 'Имя', align: 'left', value: 'phrase', sortable: false},
                ],
                data: [],
                total: 0,
                pages: [10, 50, 100, 250 , 500],
                pagination: {
                    sortBy: 'id', descending: true,
                },
                page: null,
                rowsPerPage: null,
                sortBy: null,
                descending: null,
                selected: [],
                load: false,
            }
        },

		created() {
        	if(this.showActionButtons)
				this.headers.push({text: 'Действия', align: 'left', sortable: false, width: "150px"});
		},

        watch: {
            pagination: {
                handler(v) {
                    this.nextPage(v.page - 1, v.rowsPerPage, v.sortBy, v.descending);
                    this.page = v.page - 1;
                    this.rowsPerPage = v.rowsPerPage;
                    this.sortBy = v.sortBy;
                    this.descending = v.descending;
                },
                deep: true
            }
        },

        methods: {
            nextPage(pageNumber, pageSize, sortingColumn, sortingDirection) {
                this.data = [];
                this.load = true;
                let params = {
                    containsInTraffic: this.isHave,
                    erdiTrafficUnitId: this.id,
                    pageSize: pageSize,
                    pageNumber: pageNumber,
                    sortingColumn: sortingColumn,
                    sortingDirection: this.sortTable(sortingDirection),
                };
                const config = () => ({params: params});
                this.$axios.get(this.$urls.PHRASES,config()).then(resp=>{
                    this.data = resp.data.content;
                    this.total = resp.data.totalElements;
                    this.load = false;
                }).catch(error=>{
                    console.log('error custom: ', error);
                });


            },

            sortTable(descending){
                if(descending!=null){
                    if (descending)
                        return 'DESC'
                    else return 'ASC'
                }else return descending;
            },

            add(){
                let url = `${this.$axios.defaults.baseURL}${this.$urls.QUERY_UNIT}${this.id}/add`;
                console.log('url ', url);
                console.log('array ', this.selected.map(item=>item.id));
                this.$axios.put(url,this.selected.map(item=>item.id)).then(()=>{
                    this.$router.push({name: 'trafficinfo', params: {id: this.$route.params.traffic_id}});
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
            },

            delete(){
                let url = `${this.$axios.defaults.baseURL}${this.$urls.QUERY_UNIT}${this.id}/remove`;
                this.$axios.put(url,this.selected.map(item=>item.id)).then(()=>{
                    this.nextPage(this.page,this.rowsPerPage,this.sortBy,this.descending);
                }).catch(error=>{
                    console.log('error custom: ', error);
                });
            },

			refresh() {
				this.nextPage(this.page,this.rowsPerPage,this.sortBy,this.descending);
			}
        }
    }
</script>

<style scoped>

</style>